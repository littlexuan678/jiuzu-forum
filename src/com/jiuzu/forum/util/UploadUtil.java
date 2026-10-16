package com.jiuzu.forum.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * 上传文件根目录解析工具
 *
 * <p><b>为什么需要它：</b>图片原本直接保存在 webapps 应用目录里的 {@code uploads/} 下，
 * 而每次重新部署 WAR 时 Tomcat 会先把整个应用目录删除再解压，
 * 于是历史上所有上传的图片全部消失。现在把上传根目录挪到应用之外的位置：</p>
 *
 * <ol>
 *   <li>系统属性 {@code -Dforum.upload.dir=/xxx}（优先级最高，便于自定义挂载点）</li>
 *   <li>{@code $CATALINA_BASE/forum-data/uploads}（与数据库同目录，推荐）</li>
 *   <li>兜底：应用目录内的 {@code uploads/}（本地直接跑时用）</li>
 * </ol>
 *
 * <p>外置目录由 {@link FileServlet} 负责读取，因此页面里的 URL 依旧是
 * {@code /上下文/uploads/posts/xxx.jpg}，不需要改任何数据。</p>
 *
 * @author 第九小组
 */
public final class UploadUtil {

    private UploadUtil() {
    }

    /**
     * 解析上传根目录（不存在会自动创建）
     *
     * @param webRealPath 应用根目录真实路径（可为 null）
     */
    public static File resolveRoot(String webRealPath) {
        File root = null;

        String custom = System.getProperty("forum.upload.dir", "").trim();
        if (!custom.isEmpty()) {
            root = new File(custom);
        }
        if (root == null) {
            String catalinaBase = System.getProperty("catalina.base", "").trim();
            if (!catalinaBase.isEmpty()) {
                root = new File(new File(catalinaBase, "forum-data"), "uploads");
            }
        }
        if (root == null && webRealPath != null) {
            root = new File(webRealPath, "uploads");
        }
        if (root == null) {
            return null;
        }
        if (!root.exists() && !root.mkdirs()) {
            // 创建失败时不抛异常，避免整个应用起不来；由调用方给出友好提示
            System.err.println("[九组论坛] 无法创建上传目录：" + root.getAbsolutePath());
        }
        return root;
    }

    /**
     * 确保某个子目录（posts / replies / content）存在
     *
     * @return 可用的目录；创建失败返回 null
     */
    public static File ensureSubDir(File root, String subDir) {
        if (root == null) {
            return null;
        }
        File dir = new File(root, subDir);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("[九组论坛] 无法创建上传子目录：" + dir.getAbsolutePath());
            return null;
        }
        return dir;
    }

    /**
     * 首次启动时，把仍留在应用目录 uploads/ 里的历史图片搬迁到外置目录，
     * 这样老图片不会因为下一次部署被删掉。已存在同名文件则跳过。
     *
     * @param webRealPath 应用根目录
     * @param root        外置上传根目录
     * @return 搬迁成功的数量
     */
    public static int migrateFromWebapp(String webRealPath, File root) {
        if (webRealPath == null || root == null) {
            return 0;
        }
        File legacy = new File(webRealPath, "uploads");
        if (!legacy.isDirectory() || legacy.getAbsolutePath().equals(root.getAbsolutePath())) {
            return 0;
        }
        int n = 0;
        try {
            n = copyNonExisting(legacy, root);
        } catch (IOException e) {
            System.err.println("[九组论坛] 历史图片搬迁失败：" + e.getMessage());
            return n;
        }
        if (n > 0) {
            System.out.println("[九组论坛] 已将 " + n + " 个历史上传文件搬迁到 " + root.getAbsolutePath());
        }
        return n;
    }

    private static int copyNonExisting(File srcDir, File dstDir) throws IOException {
        int n = 0;
        File[] subs = srcDir.listFiles();
        if (subs == null) {
            return 0;
        }
        for (File f : subs) {
            if (f.isDirectory()) {
                n += copyNonExisting(f, new File(dstDir, f.getName()));
            } else if (f.isFile()) {
                File target = new File(dstDir, f.getName());
                if (!target.exists()) {
                    File parent = target.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        continue;
                    }
                    try (InputStream in = Files.newInputStream(f.toPath());
                         OutputStream out = Files.newOutputStream(target.toPath())) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                    n++;
                }
            }
        }
        return n;
    }
}
