# 九组论坛（JiuZu Forum）

一个基于 **Java Web 原生技术栈** 的轻量级论坛系统，采用 MVC 分层架构。
界面风格参考 [surmon.me](https://surmon.me/)：浅色底 + 白色模块 + 蓝色主色 + 扁平小圆角。

> 课程：Java Web 开发（2026—2027 学年第一学期）
> 适用：山西工商学院 计算机信息工程学院
> 源码仓库：https://github.com/littlexuan678/jiuzu-forum

---

## 快速开始

Windows 双击 `start-forum.bat`（自动编译、部署、启动 Tomcat 并打开浏览器）；
停止时双击 `stop-forum.bat` 或直接关闭弹出的 Tomcat 黑窗口。

- 首页：`http://localhost:8080/jiuzu-forum/posts`
- 演示账号：`admin` / `admin123`
- 手动命令：`python build.py compile|war|deploy|start|stop|all`

> 注意：论坛数据存在 `WebContent/WEB-INF/data/forum.db`（SQLite），
> 首次运行由 `AppListener` 自动建表并播种演示账号；该目录不进 Git。

---

## 一、技术栈与考核点对应

| 考试要求 | 本项目中的实现位置 |
|---|---|
| 用 **Servlet** 完成用户登录、用户注册、基础数据查询的逻辑处理 | `web/LoginServlet`、`web/RegisterServlet`、`web/PostListServlet`（含板块筛选、关键词搜索、分页） |
| 用 **JDBC** 完成数据库连接，并实现单表的查询及数据管理 | `util/DBUtil`（驱动加载 + 连接获取）、`dao/` 全部 DAO（增删改查） |
| 用 **JSP + EL + JSTL** 实现页面设计 | `WebContent/WEB-INF/jsp/**` 全部页面，禁止出现 `<% %>` 脚本片段，一律使用 `${}` 与 `<c:xxx>` |
| 设计并创建 **JavaBean** 封装业务数据 | `bean/User`、`bean/Post`、`bean/Reply`、`bean/Board`、`bean/PageBean`（分页封装） |
| 遵循 **MVC 基础分层思想** | `bean`（模型）/ `dao`+`service`（数据访问与业务）/ `web`（控制器）/ `jsp`（视图） |
| 补充技术 | `filter/EncodingFilter`（中文乱码）、`filter/AuthFilter`（登录拦截）、`util/AppListener`（启动建表）、MD5 加盐密码 |

---

## 二、功能清单

**前台**
- 用户注册（用户名规则校验、密码二次确认，注册后自动登录）
- 用户登录 / 退出登录（MD5 加密比对、封禁账号拦截）
- 帖子列表：板块筛选、关键词搜索（标题 + 正文）、最新 / 热门排序、分页
- 发帖（公告区仅管理员可发）
- 帖子详情：浏览数累加、点赞 / 取消点赞
- 回复：发表回复、删除自己的回复
- 删除帖子：作者本人或管理员，级联删除回复与点赞记录
- 站点统计（帖子数 / 用户数）与热门帖 Top5

**后台（管理员）**
- 内容管理：查看全部帖子、删除任意帖子、置顶 / 取消置顶
- 用户管理：查看用户列表、封禁 / 解封、设为管理员 / 取消、删除用户（单表数据管理）

---

## 三、运行环境

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 1.8 | 需配置 `JAVA_HOME` |
| Tomcat | 8.5（Servlet 3.1 / JSP 2.3） | 也可使用 9.0 |
| 数据库 | SQLite（默认，无需安装） | 可切换 MySQL，见第六节 |
| 依赖 jar | 已放在 `WebContent/WEB-INF/lib` | JSTL 1.2、sqlite-jdbc、slf4j |

---

## 四、三种运行方式

### 方式一：一键启动（推荐演示用）

双击项目根目录的 **`start-forum.bat`**，脚本会自动完成：编译 → 部署到 Tomcat → 启动服务 → 打开浏览器。
（脚本会先探测本机 JDK 与 Tomcat，若路径不同请修改 `build.py` 顶部的 `CANDIDATE_JDKS` / `CANDIDATE_TOMCATS`）

访问地址：**http://localhost:8080/jiuzu-forum**

### 方式二：打 war 包部署

```
python build.py war
```
把生成的 `jiuzu-forum.war` 复制到 Tomcat 的 `webapps` 目录，启动 Tomcat 即可。

### 方式三：IDEA 导入（课程作业提交推荐）

1. IDEA → Open → 选择本项目目录
2. File → Project Structure → Project SDK 选 JDK 1.8
3. Run → Edit Configurations → 添加 **Tomcat Server → Local**，指定本机 Tomcat
4. Deployment → 添加 Artifact / 或部署 `WebContent` 目录，Application context 填 `/jiuzu-forum`
5. 启动，浏览器访问 http://localhost:8080/jiuzu-forum

> 首次启动会自动建表并写入初始数据（5 个板块 + 管理员账号 + 2 条示例帖），无需手动执行 SQL。

---

## 五、演示账号

| 账号 | 密码 | 角色 | 说明 |
|---|---|---|---|
| `admin` | `admin123` | 管理员 | 可进后台、发公告、置顶、删帖、封禁用户 |
| （现场注册） | 任意 6~20 位 | 普通用户 | 用于演示注册 + 登录 + 发帖流程 |

---

## 六、数据库

### 表结构

| 表 | 作用 | 字段 |
|---|---|---|
| `t_user` | 用户 | id, username, password(MD5), nickname, avatar_color, role, banned, created_time, last_login_time |
| `t_board` | 板块 | id, name, icon, description, sort_order |
| `t_post` | 帖子 | id, board_id, title, content, user_id, views, likes, pinned, created_time, updated_time |
| `t_reply` | 回复 | id, post_id, user_id, content, created_time |
| `t_like` | 点赞 | post_id + user_id 联合主键（防止重复点赞） |

建表语句见：`db/schema-sqlite.sql`、`db/schema-mysql.sql`

### 切换 MySQL（可选）

把 `db/db.properties.example` 复制为 `src/db.properties` 并改好连接信息即可，DAO 层代码不用动：

```properties
db.type=mysql
db.url=jdbc:mysql://localhost:3306/jiuzu_forum?useUnicode=true&characterEncoding=utf8
db.username=root
db.password=123456
```

---

## 七、请求路由（Servlet 映射表）

| 请求 | Servlet | 说明 |
|---|---|---|
| `GET /posts` | PostListServlet | 帖子列表（板块 / 搜索 / 排序 / 分页） |
| `GET,POST /login` | LoginServlet | 登录页与登录处理 |
| `GET,POST /register` | RegisterServlet | 注册页与注册处理 |
| `GET /logout` | LogoutServlet | 退出登录 |
| `GET /post/detail` | PostDetailServlet | 帖子详情 |
| `GET,POST /post/add` | PostAddServlet | 发帖表单与保存 |
| `POST /post/delete` | PostDeleteServlet | 删除帖子 |
| `POST /reply/add` | ReplyAddServlet | 发表回复 |
| `GET,POST /reply/delete` | ReplyDeleteServlet | 删除回复 |
| `GET /like` | LikeServlet | 点赞 / 取消点赞 |
| `GET,POST /admin/*` | AdminServlet | 后台：用户管理、内容管理 |

过滤器：`EncodingFilter`（`/*`）、`AuthFilter`（发帖 / 回复 / 点赞 / 后台）。
监听器：`AppListener`（容器启动即初始化数据库）。

---

## 八、目录结构

```
jiuzu-forum/
├── src/com/jiuzu/forum/
│   ├── bean/       JavaBean：User / Post / Reply / Board / PageBean
│   ├── util/       DBUtil(JDBC) / Md5Util / TimeUtil / AppListener
│   ├── dao/        BaseDao + UserDao / PostDao / ReplyDao / BoardDao / RowMapper
│   ├── service/    UserService / PostService / ReplyService / BoardService
│   ├── filter/     EncodingFilter / AuthFilter
│   └── web/        BaseServlet + 各业务 Servlet
├── WebContent/
│   ├── WEB-INF/
│   │   ├── web.xml          Servlet / Filter / Listener 配置
│   │   ├── lib/             依赖 jar
│   │   ├── data/forum.db    SQLite 数据文件（自动生成）
│   │   └── jsp/             页面：common / auth / post / admin / error
│   ├── static/css/style.css 样式（surmon.me 风格）
│   ├── static/images/       logo 与图标
│   └── index.jsp            入口（重定向到 /posts）
├── db/                      SQL 脚本与 MySQL 配置示例
├── build.py                 编译 / 部署 / 打 war / 启停 Tomcat
└── start-forum.bat          一键启动
```

---

## 九、答辩速查（可能被问到的问题）

- **MVC 怎么体现的？** Model=bean+dao+service，View=JSP（EL/JSTL），Controller=Servlet，web.xml 完成映射。
- **Servlet 生命周期？** `init()` 初始化一次 → 每次请求 `service()`（内部按 method 分派 doGet/doPost）→ 容器关闭 `destroy()`。
- **乱码怎么解决的？** `EncodingFilter` 统一 `UTF-8`，同时 JSP 声明 `contentType="text/html;charset=UTF-8"`。
- **登录状态怎么保持？** `HttpSession` 保存 User 对象；`AuthFilter` 对受保护 URL 统一拦截。
- **为什么用 SQLite？** 免安装，交付即可运行；DAO 层通过 `db.properties` 可切换 MySQL，JDBC 代码无需改动。
- **密码安全？** MD5 加盐后再入库，数据库中不存明文。
- **分页怎么实现的？** `PageBean<T>` 封装页码、每页条数、总数、数据列表；SQL 用 `LIMIT ? OFFSET ?`。
- **如何防止重复点赞？** `t_like` 表以 `(post_id, user_id)` 为联合主键。

---

## 十、许可

界面风格参考 surmon.me（MIT 开源项目）的设计语言，仅借鉴视觉风格，代码为本组独立编写。
