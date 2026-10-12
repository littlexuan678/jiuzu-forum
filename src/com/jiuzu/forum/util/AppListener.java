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
        } catch (Exception e) {
            System.err.println("[九组论坛] 数据库初始化失败：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // 数据库连接在每次使用后即时关闭，此处无需额外处理
    }
}
