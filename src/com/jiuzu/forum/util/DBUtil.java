package com.jiuzu.forum.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * JDBC 工具类
 *
 * <p>职责：加载数据库驱动、建立数据库连接、统一关闭资源，
 * 并在应用启动时自动完成建库建表与初始化数据。</p>
 *
 * <p>默认使用 SQLite（文件型数据库，无需额外安装服务，便于演示与交付）；
 * 若在 {@code WEB-INF/classes/db.properties} 中配置了 MySQL，
 * 则自动切换到 MySQL 驱动，体现“面向接口编程、配置驱动”的思想。</p>
 *
 * @author 第九小组
 */
public class DBUtil {

    /** SQLite 数据库连接串前缀 */
    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    /** MySQL 驱动类 */
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    /** 数据库连接地址 */
    private static String url;
    /** 数据库用户名（SQLite 不需要） */
    private static String user = "";
    /** 数据库密码（SQLite 不需要） */
    private static String password = "";

    private static boolean inited = false;

    /**
     * 初始化数据库：加载驱动、建表、写入初始数据。
     *
     * @param webRealPath WebContent 的真实磁盘路径（由 ServletContext.getRealPath 提供）
     */
    public static synchronized void init(String webRealPath) throws Exception {
        if (inited) {
            return;
        }
        Properties prop = new Properties();
        java.io.InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
        boolean useMySQL = false;
        if (in != null) {
            prop.load(in);
            String type = prop.getProperty("db.type", "sqlite").trim();
            useMySQL = "mysql".equalsIgnoreCase(type);
        }

        if (useMySQL) {
            Class.forName(MYSQL_DRIVER);
            url = prop.getProperty("db.url");
            user = prop.getProperty("db.username", "root");
            password = prop.getProperty("db.password", "");
        } else {
            Class.forName(SQLITE_DRIVER);
            url = "jdbc:sqlite:" + resolveSqliteFile(webRealPath).getAbsolutePath().replace("\\", "/");
        }

        createTables(useMySQL);
        migrate();
        insertInitData();
        inited = true;
    }

    /** 获取数据库连接 */
    public static Connection getConnection() throws SQLException {
        if (user == null || user.isEmpty()) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password);
    }

    /** 关闭 ResultSet */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** 关闭 Statement */
    public static void close(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** 关闭 Connection */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** 一次性关闭 ResultSet、Statement、Connection */
    public static void close(ResultSet rs, Statement st, Connection conn) {
        close(rs);
        close(st);
        close(conn);
    }

    /**
     * 决定 SQLite 数据库文件放在哪。
     *
     * <p>优先级：① 启动参数 -Dforum.db.file=绝对路径 ② $CATALINA_BASE/forum-data/forum.db
     * ③ 应用内 WEB-INF/data/forum.db（兜底，覆盖部署会丢数据，不推荐）。</p>
     *
     * <p>放在 webapps 外面是为了避免重新部署 WAR 时被一起删掉——历史上踩过这个坑。</p>
     */
    private static File resolveSqliteFile(String webRealPath) {
        String custom = System.getProperty("forum.db.file", "").trim();
        if (!custom.isEmpty()) {
            File f = new File(custom);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("无法创建外部数据目录：" + parent.getAbsolutePath());
            }
            return f;
        }
        String catalinaBase = System.getProperty("catalina.base", "").trim();
        if (!catalinaBase.isEmpty()) {
            File dir = new File(catalinaBase, "forum-data");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("无法创建外部数据目录：" + dir.getAbsolutePath());
            }
            return new File(dir, "forum.db");
        }
        File dir = new File(webRealPath, "WEB-INF" + File.separator + "data");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("无法创建数据目录：" + dir.getAbsolutePath());
        }
        return new File(dir, "forum.db");
    }

    /** 是否 SQLite 模式（不同数据库的主键自增写法不同） */
    private static boolean isSqlite() {
        return url != null && url.startsWith("jdbc:sqlite:");
    }

    // ------------------------------------------------------------------ 迁移

    /**
     * 数据库迁移：为已存在的旧表追加新列（如图片路径）。
     * SQLite / MySQL 的 ALTER TABLE 都不支持 "IF NOT EXISTS"，
     * 因此用 try 包裹，遇到“列已存在”的错误直接忽略。
     */
    private static void migrate() throws SQLException {
        String[] alters = {
                "ALTER TABLE t_reply ADD COLUMN image_path TEXT",
                "ALTER TABLE t_post ADD COLUMN image_path TEXT"
        };
        Connection conn = getConnection();
        try (Statement st = conn.createStatement()) {
            for (String sql : alters) {
                try {
                    st.execute(sql);
                } catch (SQLException e) {
                    String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                    if (msg.contains("duplicate") || msg.contains("already exists")
                            || msg.contains("exist")) {
                        // 列已存在，属于正常情况，忽略
                    } else {
                        throw e;
                    }
                }
            }
        } finally {
            close(conn);
        }
    }

    // ------------------------------------------------------------------ 建表

    /** 创建全部数据表（已存在则跳过） */
    private static void createTables(boolean useMySQL) throws SQLException {
        boolean sqlite = isSqlite();
        String pk = sqlite ? "INTEGER PRIMARY KEY AUTOINCREMENT" : "INT PRIMARY KEY AUTO_INCREMENT";
        String text = sqlite ? "TEXT" : "VARCHAR(2000)";

        String[] sqls = new String[]{
                // 用户表
                "CREATE TABLE IF NOT EXISTS t_user (" +
                        "id " + pk + "," +
                        "username VARCHAR(50) NOT NULL UNIQUE," +
                        "password VARCHAR(100) NOT NULL," +
                        "nickname VARCHAR(50)," +
                        "avatar_color VARCHAR(20) DEFAULT '#0088f5'," +
                        "role INT DEFAULT 0," +
                        "banned INT DEFAULT 0," +
                        "created_time BIGINT," +
                        "last_login_time BIGINT" +
                        ")",
                // 板块表
                "CREATE TABLE IF NOT EXISTS t_board (" +
                        "id " + pk + "," +
                        "name VARCHAR(50) NOT NULL UNIQUE," +
                        "icon VARCHAR(20)," +
                        "description VARCHAR(200)," +
                        "sort_order INT DEFAULT 0" +
                        ")",
                // 帖子表
                "CREATE TABLE IF NOT EXISTS t_post (" +
                        "id " + pk + "," +
                        "board_id INT NOT NULL," +
                        "title VARCHAR(200) NOT NULL," +
                        "content " + text + "," +
                        "user_id INT NOT NULL," +
                        "views INT DEFAULT 0," +
                        "likes INT DEFAULT 0," +
                        "pinned INT DEFAULT 0," +
                        "created_time BIGINT," +
                        "updated_time BIGINT" +
                        ")",
                // 回复表
                "CREATE TABLE IF NOT EXISTS t_reply (" +
                        "id " + pk + "," +
                        "post_id INT NOT NULL," +
                        "user_id INT NOT NULL," +
                        "content " + text + "," +
                        "created_time BIGINT" +
                        ")",
                // 点赞表（联合主键，防止重复点赞）
                "CREATE TABLE IF NOT EXISTS t_like (" +
                        "post_id INT NOT NULL," +
                        "user_id INT NOT NULL," +
                        "created_time BIGINT," +
                        "PRIMARY KEY (post_id, user_id)" +
                        ")"
        };

        Connection conn = getConnection();
        try (Statement st = conn.createStatement()) {
            for (String sql : sqls) {
                st.execute(sql);
            }
        } finally {
            close(conn);
        }
    }

    // ------------------------------------------------------------------ 初始数据

    /** 首次运行时写入板块、管理员与示例内容 */
    private static void insertInitData() throws SQLException {
        long now = System.currentTimeMillis();
        Connection conn = getConnection();
        try {
            // 板块（精简为三个核心板块）
            Object[][] boards = {
                    {"综合讨论", "💬", "站务闲聊、新人报道、随便聊", 5},
                    {"技术问答", "🛠", "遇到 Bug？把问题说清楚", 4},
                    {"资源分享", "📦", "好用的工具、教程、资料", 3}
            };
            for (Object[] b : boards) {
                String check = "SELECT COUNT(1) FROM t_board WHERE name=?";
                try (PreparedStatement ps = conn.prepareStatement(check)) {
                    ps.setString(1, (String) b[0]);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            continue;
                        }
                    }
                }
                String insert = "INSERT INTO t_board(name,icon,description,sort_order) VALUES(?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setString(1, (String) b[0]);
                    ps.setString(2, (String) b[1]);
                    ps.setString(3, (String) b[2]);
                    ps.setInt(4, ((Integer) b[3]));
                    ps.executeUpdate();
                }
            }

            // 管理员账号 admin / admin123
            String checkAdmin = "SELECT COUNT(1) FROM t_user WHERE username='admin'";
            boolean hasAdmin = false;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasAdmin = true;
                }
            }
            if (!hasAdmin) {
                String insertAdmin = "INSERT INTO t_user(username,password,nickname,avatar_color,role,banned,created_time) " +
                        "VALUES('admin',?,'管理员','#f56a79',1,0,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertAdmin)) {
                    ps.setString(1, Md5Util.md5("admin123"));
                    ps.setLong(2, now);
                    ps.executeUpdate();
                }
            }

            // 没有帖子时放两条示例，避免空页面
            boolean emptyPost;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(1) FROM t_post")) {
                rs.next();
                emptyPost = rs.getInt(1) == 0;
            }
            if (emptyPost) {
                int generalId = getBoardIdByName(conn, "综合讨论");
                int techId = getBoardIdByName(conn, "技术问答");
                int adminId = getAdminId(conn);
                String insertPost = "INSERT INTO t_post(board_id,title,content,user_id,views,likes,pinned,created_time,updated_time) " +
                        "VALUES(?,?,?,?,?,?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(insertPost);
                // 开版公告（置顶）
                ps.setInt(1, generalId);
                ps.setString(2, "九组论坛开版啦");
                ps.setString(3, "欢迎来到九组论坛。\n\n· 顶部导航可以切换板块，右上角可以发帖\n· 支持回复、点赞、搜索\n· 有问题可以问右下角的 AI 助手小九");
                ps.setInt(4, adminId);
                ps.setInt(5, 128);
                ps.setInt(6, 8);
                ps.setInt(7, 1);
                ps.setLong(8, now);
                ps.setLong(9, now);
                ps.executeUpdate();
                // 普通帖
                ps.setInt(1, techId > 0 ? techId : generalId);
                ps.setString(2, "说说你这学期最想搞定的一件事");
                ps.setString(3, "我先来：把这学期的课程设计从头到尾做完整，不再临时抱佛脚。\n你们呢？");
                ps.setInt(4, adminId);
                ps.setInt(5, 56);
                ps.setInt(6, 3);
                ps.setInt(7, 0);
                ps.setLong(8, now - 3600_000L);
                ps.setLong(9, now - 3600_000L);
                ps.executeUpdate();
                ps.close();
            }
        } finally {
            close(conn);
        }
    }

    /** 取管理员 ID（用于示例内容的作者） */
    private static int getAdminId(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM t_user WHERE username='admin'")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** 按板块名取 ID（示例帖引用，避免硬编码 ID） */
    private static int getBoardIdByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM t_board WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
