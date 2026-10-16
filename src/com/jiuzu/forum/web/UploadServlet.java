package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.util.UploadUtil;

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
import java.util.Locale;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * <p>POST /upload，multipart 字段名 file。通过 <b>query 参数 type</b> 区分两种用途：</p>
 * <ul>
 *   <li><b>type=image</b>（默认）：正文插图 / 头像，白名单 jpg/png/gif/webp，≤5MB，
 *       落到 {@code uploads/content/}</li>
 *   <li><b>type=file</b>：帖子附件，白名单 zip/rar/7z/pdf/doc/docx/xls/xlsx/ppt/pptx/txt/md/csv/log，
 *       ≤20MB，落到 {@code uploads/files/}</li>
 * </ul>
 *
 * <p>返回 JSON：{"success":true,"url":"...","fileName":"原名","fileSize":"1.2 MB"}。</p>
 *
 * @author 第九小组
 */
@MultipartConfig(maxFileSize = 20 * 1024 * 1024, maxRequestSize = 25 * 1024 * 1024)
public class UploadServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    /** 图片大小上限 5MB */
    private static final long MAX_IMAGE = 5L * 1024 * 1024;
    /** 附件大小上限 20MB */
    private static final long MAX_FILE = 20L * 1024 * 1024;

    private static final String[] IMAGE_EXT = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final String[] FILE_EXT = {
            ".zip", ".rar", ".7z", ".tar", ".gz",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".md", ".csv", ".log"
    };

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        User user = getLoginUser(req);
        if (user == null) {
            write(resp, false, "请先登录后再上传文件", null, null, null);
            return;
        }

        Part part;
        try {
            part = req.getPart("file");
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("SizeLimitExceeded")) {
                write(resp, false, "文件太大了，图片最多 5MB，附件最多 20MB", null, null, null);
            } else {
                write(resp, false, "服务器未开启文件上传支持（multipart 配置缺失）", null, null, null);
            }
            return;
        }
        if (part == null || part.getSize() == 0) {
            write(resp, false, "没有选择文件", null, null, null);
            return;
        }

        // 先取完 part 再读参数，避免部分容器下 multipart 参数解析时序问题
        boolean isFile = "file".equalsIgnoreCase(req.getParameter("type"));
        long max = isFile ? MAX_FILE : MAX_IMAGE;
        if (part.getSize() > max) {
            write(resp, false, isFile ? "附件不能超过 20MB" : "图片不能超过 5MB", null, null, null);
            return;
        }

        String fileName = part.getSubmittedFileName();
        String ext = extOf(fileName, isFile);
        if (ext == null) {
            write(resp, false, isFile
                    ? "仅支持 zip / rar / 7z / pdf / doc / docx / xls / xlsx / ppt / pptx / txt / md / csv / log"
                    : "仅支持 jpg / png / gif / webp 格式", null, null, null);
            return;
        }

        // 落到应用外部的持久化目录，重新部署 WAR 也不会丢失
        String subDir = isFile ? "files" : "content";
        File dir = UploadUtil.ensureSubDir(
                UploadUtil.resolveRoot(getServletContext().getRealPath("/")), subDir);
        if (dir == null) {
            write(resp, false, "上传目录创建失败，请检查权限", null, null, null);
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
            write(resp, false, "写入文件失败：" + e.getMessage(), null, null, null);
            return;
        }

        String url = req.getContextPath() + "/uploads/" + subDir + "/" + newName;
        write(resp, true, "上传成功", url, safeName(fileName), humanSize(part.getSize()));
    }

    /** 取扩展名并按用途走白名单；不含扩展名返回 null */
    private static String extOf(String fileName, boolean isFile) {
        if (fileName == null) {
            return null;
        }
        String lower = fileName.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot < 0 || dot == lower.length() - 1) {
            return null;
        }
        String ext = lower.substring(dot);
        String[] allow = isFile ? FILE_EXT : IMAGE_EXT;
        for (String e : allow) {
            if (e.equals(ext)) {
                // .jpeg 统一存成 .jpg
                return ".jpeg".equals(ext) ? ".jpg" : ext;
            }
        }
        return null;
    }

    /** 把字节数转成 1.2 MB / 356 KB 这样的可读文案 */
    private static String humanSize(long size) {
        if (size >= 1024L * 1024 * 1024) {
            return String.format(Locale.US, "%.2f GB", size / (1024.0 * 1024 * 1024));
        }
        if (size >= 1024L * 1024) {
            return String.format(Locale.US, "%.1f MB", size / (1024.0 * 1024));
        }
        if (size >= 1024L) {
            return String.format(Locale.US, "%.1f KB", size / 1024.0);
        }
        return size + " B";
    }

    /**
     * 原始文件名可能带路径（IE 会传全路径）和控制字符，这里做净化，
     * 只保留最后一段文件名，长度上限 120 字符。
     */
    private static String safeName(String fileName) {
        if (fileName == null) {
            return "";
        }
        String name = fileName.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[\\p{Cntrl}]", "").trim();
        if (name.length() > 120) {
            name = name.substring(0, 120);
        }
        return name;
    }

    private static void write(HttpServletResponse resp, boolean ok, String msg,
                             String url, String fileName, String fileSize) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":").append(ok).append(",\"message\":\"").append(json(msg)).append("\"");
        if (url != null) {
            sb.append(",\"url\":\"").append(json(url)).append("\"");
        }
        if (fileName != null) {
            sb.append(",\"fileName\":\"").append(json(fileName)).append("\"");
        }
        if (fileSize != null) {
            sb.append(",\"fileSize\":\"").append(json(fileSize)).append("\"");
        }
        sb.append("}");
        resp.getWriter().write(sb.toString());
    }

    /** 简易 JSON 字符串转义 */
    private static String json(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c < 0x20) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
