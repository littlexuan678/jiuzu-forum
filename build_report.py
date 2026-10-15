# -*- coding: utf-8 -*-
"""生成《九组论坛》Java Web 课程设计报告 .docx（个人信息以占位符呈现）。"""
import os
from docx import Document
from docx.shared import Pt, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                   "jiuzu-forum-report.docx")

CN = "宋体"
CN_HEI = "黑体"
MONO = "Consolas"

doc = Document()

# ---- 基础字体 / 页边距 ----
sec = doc.sections[0]
sec.top_margin = Cm(2.5)
sec.bottom_margin = Cm(2.5)
sec.left_margin = Cm(2.8)
sec.right_margin = Cm(2.8)

style = doc.styles["Normal"]
style.font.name = CN
style.font.size = Pt(12)            # 小四
style._element.rPr.rFonts.set(qn("w:eastAsia"), CN)


def cjk(run, name=CN):
    run.font.name = name
    rpr = run._element.get_or_add_rPr()
    rf = rpr.find(qn("w:rFonts"))
    if rf is None:
        rf = OxmlElement("w:rFonts")
        rpr.append(rf)
    rf.set(qn("w:eastAsia"), name)
    rf.set(qn("w:ascii"), name)
    rf.set(qn("w:hAnsi"), name)


def set_heading_cn(level):
    st = doc.styles["Heading %d" % level]
    st.font.name = CN_HEI
    st._element.rPr.rFonts.set(qn("w:eastAsia"), CN_HEI)
    st.font.color.rgb = RGBColor(0x1F, 0x49, 0x7D)


for lv in (1, 2, 3):
    set_heading_cn(lv)

# ---- 工具函数 ----


def para(text="", size=12, bold=False, align=None, font=CN, color=None,
         space_after=6, first_indent=None, line=None):
    p = doc.add_paragraph()
    if align is not None:
        p.alignment = align
    if first_indent:
        p.paragraph_format.first_line_indent = first_indent
    p.paragraph_format.space_after = Pt(space_after)
    if line:
        p.paragraph_format.line_spacing = line
    if text:
        r = p.add_run(text)
        r.font.size = Pt(size)
        r.bold = bold
        if color:
            r.font.color.rgb = color
        cjk(r, font)
    return p


def bullet(text, size=12):
    p = doc.add_paragraph(style="List Bullet")
    r = p.add_run(text)
    r.font.size = Pt(size)
    cjk(r)
    return p


def code_block(text):
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Cm(0.5)
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(8)
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:fill"), "F2F4F7")
    p._p.get_or_add_pPr().append(shd)
    r = p.add_run(text)
    r.font.name = MONO
    r.font.size = Pt(9.5)
    r._element.rPr.rFonts.set(qn("w:eastAsia"), MONO)
    return p


def table(headers, rows, widths=None):
    t = doc.add_table(rows=1, cols=len(headers))
    t.style = "Light Grid Accent 1"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr = t.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = ""
        rp = hdr[i].paragraphs[0].add_run(h)
        rp.bold = True
        rp.font.size = Pt(11)
        cjk(rp, CN_HEI)
    for row in rows:
        cells = t.add_row().cells
        for i, v in enumerate(row):
            cells[i].text = ""
            rp = cells[i].paragraphs[0].add_run(str(v))
            rp.font.size = Pt(10.5)
            cjk(rp)
    if widths:
        for i, w in enumerate(widths):
            for r in t.rows:
                r.cells[i].width = Cm(w)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t


# =================== 封面 ===================
for _ in range(3):
    doc.add_paragraph()
para("课 程 设 计 报 告", size=26, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER,
     font=CN_HEI, color=RGBColor(0x1F, 0x49, 0x7D), space_after=10)
para("九组论坛 —— 基于 Java Web 的在线论坛系统", size=16, bold=True,
     align=WD_ALIGN_PARAGRAPH.CENTER, color=RGBColor(0x33, 0x33, 0x33), space_after=40)

info = [
    ("课程名称", "Java Web 程序设计（课程设计）"),
    ("学    院", "【学院名称】"),
    ("专    业", "【专业名称】"),
    ("班    级", "【班级】"),
    ("学    号", "【学号】"),
    ("姓    名", "【姓名】"),
    ("组    别", "第九组（九组）"),
    ("指导教师", "【指导教师】"),
    ("完成日期", "2026 年   月"),
]
t = doc.add_table(rows=len(info), cols=2)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
t.columns[0].width = Cm(4)
t.columns[1].width = Cm(9)
for i, (k, v) in enumerate(info):
    c0, c1 = t.rows[i].cells
    c0.text = ""; c1.text = ""
    r0 = c0.paragraphs[0].add_run(k); r0.font.size = Pt(13); r0.bold = True; cjk(r0, CN_HEI)
    r1 = c1.paragraphs[0].add_run(v); r1.font.size = Pt(13); cjk(r1)
    c0.width = Cm(4); c1.width = Cm(9)

doc.add_page_break()

# =================== 目录 ===================
para("目  录", size=16, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, font=CN_HEI,
     color=RGBColor(0x1F, 0x49, 0x7D), space_after=10)
toc = doc.add_paragraph()
run = toc.add_run()
f1 = OxmlElement("w:fldChar"); f1.set(qn("w:fldCharType"), "begin")
it = OxmlElement("w:instrText"); it.set(qn("xml:space"), "preserve"); it.text = 'TOC \\o "1-3" \\h \\z \\u'
f2 = OxmlElement("w:fldChar"); f2.set(qn("w:fldCharType"), "separate")
ft = OxmlElement("w:t"); ft.text = "（在 Word 中右键此区域 →“更新域”即可生成目录）"
f3 = OxmlElement("w:fldChar"); f3.set(qn("w:fldCharType"), "end")
run._r.append(f1); run._r.append(it); run._r.append(f2); run._r.append(ft); run._r.append(f3)

doc.add_page_break()

# =================== 正文 ===================
para("九组论坛 —— 基于 Java Web 的在线论坛系统课程设计报告", size=18, bold=True,
     align=WD_ALIGN_PARAGRAPH.CENTER, font=CN_HEI, color=RGBColor(0x1F, 0x49, 0x7D), space_after=14)
doc.add_paragraph()

# 1 项目概述
doc.add_heading("1 项目概述", level=1)
doc.add_heading("1.1 项目背景", level=2)
para("随着校内学习交流需求的增长，一个轻量、易用、风格现代的论坛系统有助于同学之间分享资料、答疑解惑。"
     "本项目“九组论坛”是一个面向课程设计的在线论坛，采用标准 Java Web 技术栈实现，"
     "并在传统论坛基础上集成了基于大模型的 AI 帮写能力，提升发帖与回复的体验。",
     first_indent=Cm(0.74), line=1.4)
doc.add_heading("1.2 项目目标", level=2)
bullet("实现一个完整的 BBS 论坛：用户注册/登录、板块浏览、发帖、回复、点赞、置顶。")
bullet("提供后台管理：数据统计、板块管理、回复管理。")
bullet("集成 DeepSeek 大模型，在发帖与回复场景提供“AI 帮写”草稿生成。")
bullet("界面采用现代化青色主题、全屏 Hero、浮动胶囊导航与悬浮操作按钮，支持桌面与移动端。")
doc.add_heading("1.3 项目特色", level=2)
bullet("严格贴合考试要求的技术栈（Servlet/JSP/EL/JSTL/JavaBean/JDBC/MVC），无第三方 Web 框架。")
bullet("AI 帮写使用 JDK 原生 HttpURLConnection 直连 DeepSeek，零额外依赖。")
bullet("响应式布局，移动端体验良好。")

# 2 技术栈
doc.add_heading("2 技术栈与开发环境", level=1)
doc.add_heading("2.1 技术选型", level=2)
table(["层次", "技术", "说明"],
      [["表现层", "JSP 2.3 + EL + JSTL 1.2", "页面渲染、视图逻辑"],
       ["控制层", "Servlet 3.1", "请求分发、业务调度"],
       ["业务层", "Service / JavaBean", "业务逻辑封装"],
       ["持久层", "JDBC + SQLite", "数据访问，零 ORM 框架"],
       ["架构模式", "MVC", "模型-视图-控制器分离"],
       ["AI 能力", "DeepSeek API", "HttpURLConnection 原生调用"],
       ["服务器", "Tomcat 8.5", "Servlet 容器"],
       ["JDK", "Java 8", "运行环境"]],
      widths=[2.5, 5.5, 6])
doc.add_heading("2.2 开发环境", level=2)
bullet("操作系统：Windows 11 / 部署于 Tencent Cloud Ubuntu 服务器。")
bullet("开发工具：IntelliJ IDEA / VS Code。")
bullet("数据库：SQLite（单文件 forum.db），便于迁移与备份。")
bullet("构建方式：本地 JDK 编译打包为 WAR，部署至 Tomcat webapps。")

# 3 需求分析
doc.add_heading("3 系统需求分析", level=1)
doc.add_heading("3.1 功能需求", level=2)
table(["模块", "功能点"],
      [["用户", "注册、登录、退出、权限区分（普通用户/管理员）"],
       ["板块", "三板块展示（综合讨论/技术问答/资源分享）、导航筛选"],
       ["帖子", "发帖、列表、详情、点赞、置顶、删除"],
       ["回复", "发表回复、查看回复、删除回复"],
       ["AI 帮写", "发帖/回复场景调用 DeepSeek 生成草稿并回填"],
       ["后台", "数据统计、板块增删改、回复管理"]],
      widths=[3, 12])
doc.add_heading("3.2 非功能需求", level=2)
bullet("可用性：界面友好，关键操作有明确反馈。")
bullet("安全性：管理员操作需登录鉴权；用户输入经 HTML 转义防 XSS。")
bullet("可维护性：MVC 分层清晰，DAO/Service/Servlet 职责单一。")

# 4 总体设计
doc.add_heading("4 系统总体设计", level=1)
doc.add_heading("4.1 架构设计（MVC）", level=2)
para("系统采用经典 MVC 三层架构：", first_indent=Cm(0.74))
code_block(
    "Browser\n"
    "  │  HTTP 请求\n"
    "  ▼\n"
    "Servlet (Controller)  ── 解析参数 / 鉴权 / 转发\n"
    "  │\n"
    "  ▼\n"
    "Service (Model 业务) ── 业务规则\n"
    "  │\n"
    "  ▼\n"
    "DAO (Model 数据) ────── JDBC ────── SQLite (forum.db)\n"
    "  │\n"
    "  ▼\n"
    "JSP + EL/JSTL (View) ── 渲染响应")
doc.add_heading("4.2 目录结构", level=2)
code_block(
    "jiuzu-forum/\n"
    "├─ src/com/jiuzu/forum/\n"
    "│  ├─ bean/      User / Board / Post / Reply\n"
    "│  ├─ dao/       UserDao / BoardDao / PostDao / ReplyDao\n"
    "│  ├─ service/   UserService / BoardService / PostService\n"
    "│  ├─ util/      DBUtil / DeepSeekUtil / EncodingFilter / AppListener\n"
    "│  └─ web/       Login/Logout/Post*/Reply*/Board*/Admin*/Ai* Servlet\n"
    "└─ WebContent/\n"
    "   ├─ WEB-INF/jsp/   header / post / admin / ai ...\n"
    "   └─ static/css/    style.v8.css / admin.css")
doc.add_heading("4.3 数据库设计", level=2)
para("核心数据表如下：", first_indent=Cm(0.74))
table(["表名", "主要字段", "说明"],
      [["t_user", "id, username, password, nickname, is_admin, created_time", "用户与权限"],
       ["t_board", "id, name, icon, description, sort", "板块（3 个）"],
       ["t_post", "id, board_id, title, content, user_id, views, likes, pinned, created_time", "帖子"],
       ["t_reply", "id, post_id, user_id, content, created_time", "回复"]],
      widths=[2.5, 8, 4.5])
para("外键关系：t_post.board_id → t_board.id；t_reply.post_id → t_post.id；"
     "t_post.user_id / t_reply.user_id → t_user.id。", first_indent=Cm(0.74), line=1.4)

# 5 功能模块
doc.add_heading("5 功能模块设计与实现", level=1)
doc.add_heading("5.1 用户模块", level=2)
para("提供注册、登录、退出。登录后 Session 保存用户对象，过滤器对需要鉴权的操作（发帖、回复、后台）"
     "进行拦截与重定向。管理员（admin）可进入后台。", first_indent=Cm(0.74), line=1.4)
doc.add_heading("5.2 板块模块", level=2)
para("论坛精简为三个板块：综合讨论、技术问答、资源分享。InitListener 在库为空时写入种子数据，"
     "保证首次启动即可见完整结构。", first_indent=Cm(0.74), line=1.4)
doc.add_heading("5.3 帖子模块", level=2)
bullet("列表：按板块筛选、置顶优先、分页展示。")
bullet("详情：正文 + 回复列表 + 回复框 + 点赞/删除。")
bullet("发帖：标题/正文 + 板块选择，含表单校验。")
doc.add_heading("5.4 回复模块", level=2)
para("在帖子详情页发表回复，支持 Ctrl/⌘+Enter 快捷提交；回复内容同样经转义后存储与展示。",
     first_indent=Cm(0.74), line=1.4)
doc.add_heading("5.5 AI 帮写模块", level=2)
para("发帖表单与回复框各设“✨ AI 帮写”按钮，输入主题/要求后调用 DeepSeek 生成草稿并自动回填输入框，"
     "用户可修改后再提交。后端复用统一的 AI 调用层。", first_indent=Cm(0.74), line=1.4)
doc.add_heading("5.6 后台管理模块", level=2)
bullet("数据统计：用户/帖子/回复/板块总数 + 今日新增。")
bullet("板块管理：新增、编辑、删除（删除可把帖子合并到其他板块）。")
bullet("回复管理：全部回复列表（含所属帖子/板块/作者）+ 单条删除。")

# 6 关键技术
doc.add_heading("6 关键技术与实现要点", level=1)
doc.add_heading("6.1 MVC 分层实现", level=2)
para("所有请求经 BaseServlet 统一分发；业务通过 Service 调用 DAO，DAO 仅负责 SQL 与映射，"
     "视图由 JSP + EL/JSTL 渲染，职责清晰、易于测试。", first_indent=Cm(0.74), line=1.4)
doc.add_heading("6.2 JDBC 工具类", level=2)
para("DBUtil 负责加载驱动、获取连接、初始化种子数据，并集中管理 SQLite 单文件数据库路径。",
     first_indent=Cm(0.74), line=1.4)
doc.add_heading("6.3 统一编码过滤器", level=2)
para("EncodingFilter 对所有请求/响应设置 UTF-8，并对动态请求添加 no-cache 头，避免页面缓存导致更新不生效。",
     first_indent=Cm(0.74), line=1.4)
doc.add_heading("6.4 DeepSeek 原生接入（零依赖）", level=2)
para("关键代码使用 JDK 原生 HttpURLConnection 直连 api.deepseek.com，手工拼装 JSON 与解析响应，"
     "不引入任何第三方 HTTP / JSON 库，完全符合考试技术栈要求：", first_indent=Cm(0.74), line=1.4)
code_block(
    "URL u = new URL(BASE_URL + \"/chat/completions\");\n"
    "HttpURLConnection c = (HttpURLConnection) u.openConnection();\n"
    "c.setRequestMethod(\"POST\");\n"
    "c.setRequestProperty(\"Content-Type\", \"application/json\");\n"
    "c.setRequestProperty(\"Authorization\", \"Bearer \" + API_KEY);\n"
    "c.setDoOutput(true);\n"
    "try (OutputStream os = c.getOutputStream()) {\n"
    "    os.write(buildJson(messages).getBytes(StandardCharsets.UTF_8));\n"
    "}\n"
    "String reply = readBody(c.getInputStream());   // 解析 choices[0].message.content")
doc.add_heading("6.5 JSTL / EL 视图渲染", level=2)
para("列表与条件渲染使用 <c:forEach>/<c:if>/<c:choose> 等 JSTL 标签配合 EL 表达式，"
     "用户输入展示统一使用 <c:out> 转义，防止 XSS。", first_indent=Cm(0.74), line=1.4)

# 7 部署
doc.add_heading("7 系统部署", level=1)
doc.add_heading("7.1 本地部署", level=2)
bullet("将项目编译为 WAR（build.py war）。")
bullet("复制至 Tomcat/webapps，启动 Tomcat，访问 /jiuzu-forum。")
bullet("首次启动 InitListener 自动建表并写入种子数据。")
doc.add_heading("7.2 云服务器部署", level=2)
bullet("服务器：Tencent Cloud Ubuntu + Tomcat 8.5 + JDK 8。")
bullet("通过 WAR 上传替换、重启服务完成发布。")
bullet("数据库 forum.db 可备份与还原，便于版本迁移。")

# 8 分工
doc.add_heading("8 模块分工", level=1)
para("本课程设计由第九组完成，成员分工如下（请按实际填写）：", first_indent=Cm(0.74))
table(["姓名", "学号", "负责模块"],
      [["【姓名1】", "【学号1】", "需求分析、数据库设计与实现"],
       ["【姓名2】", "【学号2】", "前端页面（JSP/主题/响应式）"],
       ["【姓名3】", "【学号3】", "后端 Servlet/Service/DAO 逻辑"],
       ["【姓名4】", "【学号4】", "AI 帮写接入、后台管理与部署"]],
      widths=[3.5, 4, 7.5])

# 9 测试
doc.add_heading("9 测试与运行", level=1)
bullet("功能测试：注册/登录、发帖/回复、点赞/置顶、后台各页面均通过。")
bullet("AI 测试：POST /ai/chat 返回真实 DeepSeek 回复；发帖/回复帮写可正常生成草稿。")
bullet("兼容测试：桌面端 Chrome/Edge 与移动端浏览器均正常显示。")

# 10 总结
doc.add_heading("10 总结与展望", level=1)
para("九组论坛完整覆盖了 Java Web 课程的核心知识点：Servlet/JSP 请求处理、EL/JSTL 视图渲染、"
     "JavaBean 模型、JDBC 数据访问与 MVC 分层架构，并在此基础上扩展了 AI 帮写能力。"
     "后续可进一步引入全文搜索、消息通知、Markdown 编辑等功能。",
     first_indent=Cm(0.74), line=1.4)

# 附录
doc.add_heading("附录 A 访问信息与账号", level=1)
bullet("线上地址：http://129.211.29.190/jiuzu-forum")
bullet("管理员账号：admin / admin123")
bullet("AI 配置：WEB-INF/config.properties（deepseek.api.key 等）")

doc.add_heading("附录 B 核心依赖说明", level=1)
bullet("sqlite-jdbc：SQLite JDBC 驱动（唯一运行时数据库依赖）。")
bullet("jstl / standard：JSTL 标签库。")
bullet("DeepSeek 调用：JDK 原生，无额外依赖。")

# ---- 页脚页码 ----
footer = sec.footer
fp = footer.paragraphs[0]
fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = fp.add_run("第 ")
cjk(r)
ff1 = OxmlElement("w:fldChar"); ff1.set(qn("w:fldCharType"), "begin")
fin = OxmlElement("w:instrText"); fin.set(qn("xml:space"), "preserve"); fin.text = "PAGE"
ff2 = OxmlElement("w:fldChar"); ff2.set(qn("w:fldCharType"), "end")
r._r.append(ff1); r._r.append(fin); r._r.append(ff2)
r2 = fp.add_run(" 页")
cjk(r2)

doc.save(OUT)
print("SAVED:", OUT)
