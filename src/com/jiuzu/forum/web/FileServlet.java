package com.jiuzu.forum.web;

import com.jiuzu.forum.util.UploadUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;

/**
 * 上传文件的读取控制器
 *
 * <p>把 {@code /uploads/*} 的请求映射到<b>应用外部</b>的持久化目录
 * （{@code $CATALINA_BASE/forum-data/uploads}），
 * 这样重新部署 WAR 时 Tomcat 删除应用目录也不会再弄丢图片。</p>
 *
 * <p>如果外置目录里没有该文件，会回退到应用目录内的 {@code uploads/}，
 * 用于兼容尚未搬迁的历史文件和本地直接运行的场景。</p>
 *
 * @author 第九小组
 */
public class FileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 外置持久化上传根目录 */
    private File externalRoot;
    /** 应用目录，用于回退读取 */
    private File webRoot;

    @Override
    public void init() throws ServletException {
        String realPath = getServletContext().getRealPath("/");
        webRoot = realPath == null ? null : new File(realPath);
        externalRoot = UploadUtil.resolveRoot(realPath);
        try {
            int n = UploadUtil.migrateFromWebapp(realPath, externalRoot);
            if (n > 0) {
                System.out.println("[九组论坛] FileServlet 启动搬迁 " + n + " 个历史文件");
            }
        } catch (Exception ignored) {
            // 搬迁失败不影响对外服务
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String rel = req.getPathInfo();
        if (rel == null || rel.equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(rel, "UTF-8");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // 目录穿越防护
        String clean = decoded.replace('\\', '/');
        if (clean.contains("..") || clean.contains("\0")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        while (clean.startsWith("/")) {
            clean = clean.substring(1);
        }

        File target = null;
        if (externalRoot != null) {
            File candidate = new File(externalRoot, clean);
            // 二次校验，确保解析后仍在根目录之内
            if (isInside(externalRoot, candidate) && candidate.isFile()) {
                target = candidate;
            }
        }
        if (target == null && webRoot != null) {
            File legacy = new File(new File(webRoot, "uploads"), clean);
            if (isInside(new File(webRoot, "uploads"), legacy) && legacy.isFile()) {
                target = legacy;
            }
        }
        if (target == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String name = target.getName().toLowerCase();
        String type = getServletContext().getMimeType(target.getName());
        if (type == null) {
            if (name.endsWith(".png")) {
                type = "image/png";
            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                type = "image/jpeg";
            } else if (name.endsWith(".gif")) {
                type = "image/gif";
            } else if (name.endsWith(".webp")) {
                type = "image/webp";
            } else {
                type = "application/octet-stream";
            }
        }
        resp.setContentType(type);
        resp.setContentLengthLong(target.length());
        // 图片内容不会变，允许浏览器与 CDN 缓存一天
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setDateHeader("Last-Modified", target.lastModified());

        // 附件目录（uploads/files）一律以下载方式返回，避免 html / svg 之类被浏览器直接解析执行
        if (clean.startsWith("files/")) {
            String raw = req.getParameter("name");
            if (raw == null || raw.trim().isEmpty()) {
                raw = target.getName();
            }
            // 防 CRLF 注入：响应头里不能出现换行和引号
            raw = raw.replaceAll("[\\r\\n\"]", "").trim();
            if (raw.isEmpty()) {
                raw = target.getName();
            }
            String ascii = raw.replaceAll("[^\\x20-\\x7E]", "_");
            String encoded;
            try {
                encoded = java.net.URLEncoder.encode(raw, "UTF-8").replace("+", "%20");
            } catch (IOException e) {
                encoded = ascii;
            }
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded);
        }

        try (InputStream in = Files.newInputStream(target.toPath());
             OutputStream out = resp.getOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    /** 判断 child 是否真的位于 parent 目录内（防止 ../ 穿越） */
    private static boolean isInside(File parent, File child) {
        try {
            String p = parent.getCanonicalPath();
            String c = child.getCanonicalPath();
            return c.equals(p) || c.startsWith(p + File.separator);
        } catch (IOException e) {
            return false;
        }
    }
}
