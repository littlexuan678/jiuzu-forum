package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.filter.AuthFilter;
import com.jiuzu.forum.util.UploadUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Servlet 基类
 *
 * <p>抽取了控制器里反复用到的动作：获取登录用户、转发到 JSP、重定向、
 * 读取请求参数、设置错误提示等，让各个业务 Servlet 只关心自己的流程。</p>
 *
 * @author 第九小组
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** JSP 页面统一放在 /WEB-INF/jsp 下，避免被直接访问 */
    protected static final String JSP = "/WEB-INF/jsp/";

    /** 允许上传的图片扩展名（小写） */
    private static final String[] IMG_EXT = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    /** 单张图片大小上限：5MB */
    private static final long MAX_IMG_SIZE = 5L * 1024 * 1024;

    /** 获取当前登录用户，未登录返回 null */
    protected User getLoginUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute(AuthFilter.SESSION_USER);
    }

    /** 要求必须登录，否则抛出异常由调用方处理 */
    protected User requireLogin(HttpServletRequest req) {
        User user = getLoginUser(req);
        if (user == null) {
            throw new IllegalStateException("请先登录");
        }
        return user;
    }

    /** 转发到指定的 JSP 页面 */
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        req.getRequestDispatcher(JSP + jsp).forward(req, resp);
    }

    /** 重定向（自动带上项目上下文路径） */
    protected void redirect(HttpServletResponse resp, HttpServletRequest req, String path)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** 读取 int 参数，缺省返回默认值 */
    protected int getIntParam(HttpServletRequest req, String name, int def) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /** 读取字符串参数，缺省返回空串 */
    protected String getStringParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    /** 设置错误提示（JSP 中用 ${error} 显示） */
    protected void setError(HttpServletRequest req, String msg) {
        req.setAttribute("error", msg);
    }

    /** 设置操作成功提示（JSP 中用 ${message} 显示） */
    protected void setMessage(HttpServletRequest req, String msg) {
        req.setAttribute("message", msg);
    }

    // ---------------------------------------------------------- 文件上传相关

    /** 请求是否为 multipart/form-data（带文件上传） */
    protected boolean isMultipart(HttpServletRequest req) {
        String ct = req.getContentType();
        return ct != null && ct.toLowerCase().contains("multipart/");
    }

    /** 将字符串解析为 int，失败返回默认值 */
    protected int parseInt(String s, int def) {
        if (s == null) {
            return def;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /** 读取 multipart 表单里的文本字段（非文件 part），缺省返回空串 */
    protected String getMultipartString(HttpServletRequest req, String name)
            throws IOException, ServletException {
        Part p = req.getPart(name);
        if (p == null) {
            return "";
        }
        // 文件型 part 带有 filename，跳过
        if (p.getSubmittedFileName() != null && !p.getSubmittedFileName().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), "UTF-8"))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                // readLine 会吃掉换行符，这里必须补回去，否则正文的回车分段全部丢失
                if (first) {
                    first = false;
                } else {
                    sb.append('\n');
                }
                sb.append(line);
            }
        }
        return sb.toString().trim();
    }

    /**
     * 保存上传的图片文件，返回可在页面直接引用的相对路径（如 /uploads/replies/123_ab.jpg）。
     * 未选择文件时返回 null；类型或大小非法时抛出 IllegalArgumentException。
     */
    protected String saveMultipartImage(HttpServletRequest req, String partName, String subDir)
            throws IOException, ServletException {
        Part p = req.getPart(partName);
        if (p == null) {
            return null;
        }
        String fileName = p.getSubmittedFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            return null; // 没选文件
        }
        fileName = fileName.trim();
        String lower = fileName.toLowerCase();
        boolean okExt = false;
        String ext = "";
        for (String e : IMG_EXT) {
            if (lower.endsWith(e)) {
                okExt = true;
                ext = e;
                break;
            }
        }
        if (!okExt) {
            throw new IllegalArgumentException("仅支持 jpg / png / gif / webp 图片");
        }
        if (p.getSize() > MAX_IMG_SIZE) {
            throw new IllegalArgumentException("图片不能超过 5MB");
        }
        String newName = System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;
        // 落到应用外部的持久化目录（$CATALINA_BASE/forum-data/uploads/<subDir>），
        // 与 UploadServlet 保持一致，重新部署 WAR 不会丢图
        File dir = UploadUtil.ensureSubDir(
                UploadUtil.resolveRoot(getServletContext().getRealPath("/")), subDir);
        if (dir == null) {
            throw new IOException("无法创建上传目录");
        }
        File out = new File(dir, newName);
        try (InputStream in = p.getInputStream();
             OutputStream os = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                os.write(buf, 0, n);
            }
        }
        return "/uploads/" + subDir + "/" + newName;
    }
}
