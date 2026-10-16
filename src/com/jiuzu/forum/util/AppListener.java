package com.jiuzu.forum.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 应用启动监听器
 *
 * <p>在 Web 容器启动时完成数据库的初始化（建表、写入初始数据），
 * 这样项目部署后无需手动执行 SQL 脚本即可直接运行。</p>
 *
 * @author 第九小组
 */
public class AppListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        try {
            String realPath = sce.getServletContext().getRealPath("/");
            DBUtil.init(realPath);
            System.out.println("[九组论坛] 数据库初始化完成：" + realPath);

            // 把过去存在应用目录内的历史上传文件搬到本地外的持久化目录，
            // 避免后续重新部署 WAR 时图片被一起删掉
            try {
                int n = UploadUtil.migrateFromWebapp(realPath, UploadUtil.resolveRoot(realPath));
                if (n > 0) {
                    System.out.println("[九组论坛] 已搬迁 " + n + " 个历史上传文件到外部目录");
                }
            } catch (Exception e) {
                System.err.println("[九组论坛] 历史上传文件搬迁异常：" + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[九组论坛] 数据库初始化失败：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // 数据库连接在每次使用后即时关闭，此处无需额外处理
    }
}
