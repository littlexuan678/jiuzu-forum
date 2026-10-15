package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

/**
 * 正文图片上传控制器（供编辑器「插入到光标处」使用）
 *
 * <p>POST /upload，multipart 字段名 file，返回 JSON：
 * {"success":true,"url":"/jiuzu-forum/uploads/content/xxx.png"}
 * 失败返回 {"success":false,"message":"原因"}。</p>
 *
 * @author 第九小组
 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class UploadServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        User user = getLoginUser(req);
        if (user == null) {
            write(resp, false, "请先登录后再上传图片", null);
            return;
        }

        Part part;
        try {
            part = req.getPart("file");
        } catch (IllegalStateException e) {
            write(resp, false, "服务器未开启文件上传支持（multipart 配置缺失）", null);
            return;
        }
        if (part == null || part.getSize() == 0) {
            write(resp, false, "没有选择图片", null);
            return;
        }
        if (part.getSize() > 5 * 1024 * 1024) {
            write(resp, false, "图片不能超过 5MB", null);
            return;
        }

        String fileName = part.getSubmittedFileName();
        String ext = extOf(fileName);
        if (ext == null) {
            write(resp, false, "仅支持 jpg / png / gif / webp 格式", null);
            return;
        }

        // 直接落到 WebContent/uploads/content 下，供 /uploads/** 静态访问
        String dirPath = getServletContext().getRealPath("/uploads/content");
        if (dirPath == null) {
            write(resp, false, "无法定位上传目录", null);
            return;
        }
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            write(resp, false, "上传目录创建失败，请检查权限", null);
            return;
        }

        String newName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        File dest = new File(dir, newName);
        try (InputStream in = part.getInputStream(); OutputStream out = Files.newOutputStream(dest.toPath())) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
        } catch (IOException e) {
            write(resp, false, "写入文件失败：" + e.getMessage(), null);
            return;
        }

        String url = req.getContextPath() + "/uploads/content/" + newName;
        write(resp, true, "上传成功", url);
    }

    /** 取扩展名，附带类型白名单 */
    private static String extOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return ".jpg";
        }
        if (lower.endsWith(".png")) {
            return ".png";
        }
        if (lower.endsWith(".gif")) {
            return ".gif";
        }
        if (lower.endsWith(".webp")) {
            return ".webp";
        }
        return null;
    }

    private static void write(HttpServletResponse resp, boolean ok, String msg, String url) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":").append(ok).append(",\"message\":\"")
                .append(msg == null ? "" : msg.replace("\"", "'")).append("\"");
        if (url != null) {
            sb.append(",\"url\":\"").append(url).append("\"");
        }
        sb.append("}");
        resp.getWriter().write(sb.toString());
    }
}
