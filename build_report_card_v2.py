# -*- coding: utf-8 -*-
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

CN = "宋体"
HEI = "黑体"

doc = Document()

# ---- 页边距 ----
for s in doc.sections:
    s.top_margin = Cm(1.8); s.bottom_margin = Cm(1.8)
    s.left_margin = Cm(2.0); s.right_margin = Cm(2.0)

def set_cn(run, font=CN):
    run.font.name = font
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.find(qn('w:rFonts'))
    if rfonts is None:
        rfonts = OxmlElement('w:rFonts'); rpr.append(rfonts)
    rfonts.set(qn('w:eastAsia'), font)
    rfonts.set(qn('w:ascii'), font)
    rfonts.set(qn('w:hAnsi'), font)

def para(text="", size=10.5, bold=False, align=None, font=CN, space_before=0, space_after=4, line=None, color=None):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    if align is not None: p.alignment = align
    if line: p.paragraph_format.line_spacing = line
    if text:
        r = p.add_run(text); r.font.size = Pt(size); r.bold = bold
        set_cn(r, font)
        if color: r.font.color.rgb = color
    return p

def heading(text, size=12, space_before=8):
    p = para(text, size=size, bold=True, font=HEI, space_before=space_before, space_after=4)
    return p

def set_cell(cell, text, size=10, bold=False, align=WD_ALIGN_PARAGRAPH.LEFT, font=CN, color=None):
    cell.text = ""
    p = cell.paragraphs[0]
    p.alignment = align
    p.paragraph_format.space_after = Pt(0); p.paragraph_format.space_before = Pt(0)
    r = p.add_run(text)
    r.font.size = Pt(size); r.bold = bold
    set_cn(r, font)
    if color: r.font.color.rgb = color
    cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER

def shade(cell, fill):
    tcPr = cell._tc.get_or_add_tcPr()
    sh = OxmlElement('w:shd'); sh.set(qn('w:val'),'clear'); sh.set(qn('w:fill'), fill)
    tcPr.append(sh)

def borders(table, sz=4, color="000000"):
    tbl = table._tbl
    tblPr = tbl.tblPr
    borders = OxmlElement('w:tblBorders')
    for edge in ('top','left','bottom','right','insideH','insideV'):
        e = OxmlElement('w:'+edge)
        e.set(qn('w:val'),'single'); e.set(qn('w:sz'),str(sz)); e.set(qn('w:color'),color)
        borders.append(e)
    tblPr.append(borders)

# ============ 封面标题 ============
para("山西工商学院计算机信息工程学院", size=14, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, font=HEI, space_before=4, space_after=2)
para("《Java Web开发》期末考试报告单", size=16, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, font=HEI, space_after=2)
para("2026—2027 学年 第一学期", size=12, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, font=HEI, space_after=8)

# ============ 基本信息表 ============
info = doc.add_table(rows=2, cols=4)
info.alignment = WD_TABLE_ALIGNMENT.CENTER
borders(info)
set_cell(info.rows[0].cells[0], "班级", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
set_cell(info.rows[0].cells[1], "25级数据科学与大数据技术（专升本）2班")
set_cell(info.rows[0].cells[2], "组号", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
set_cell(info.rows[0].cells[3], "第九小组（9）")
set_cell(info.rows[1].cells[0], "课程", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
set_cell(info.rows[1].cells[1], "《Java Web开发》")
set_cell(info.rows[1].cells[2], "教师", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
set_cell(info.rows[1].cells[3], "【教师姓名】")
para("（学生黑色签字笔填写 / 教师黑色签字笔填写）", size=8.5, color=RGBColor(0x88,0x88,0x88), space_before=2, space_after=6)

# ============ 成员分工表（六人六模块） ============
heading("小组成员及模块分工", size=12)
members = [
    ("1", "【学号1】", "【姓名1】", "用户与权限模块", "注册 / 登录 / 注销 / 会话维持 / 角色区分"),
    ("2", "【学号2】", "【姓名2】", "板块与导航模块", "三个板块展示 / SVG 图标 / 全局头部与浮动导航"),
    ("3", "【学号3】", "【姓名3】", "帖子模块", "列表排序搜索 / 详情 / 发帖 / 点赞 / 浏览量"),
    ("4", "【学号4】", "【姓名4】", "回复模块", "发表回复 / 后台回复统一管理"),
    ("5", "【学号5】", "【姓名5】", "后台管理模块", "数据概览 / 板块管理 / 用户与内容管理"),
    ("6", "【学号6】", "【姓名6】", "AI 助手模块", "DeepSeek 问答 / 发帖·回复 AI 帮写"),
]
mt = doc.add_table(rows=len(members)+1, cols=5)
mt.alignment = WD_TABLE_ALIGNMENT.CENTER
borders(mt)
hdr = ["序号", "学号", "姓名", "负责模块", "主要工作"]
for i, h in enumerate(hdr):
    set_cell(mt.rows[0].cells[i], h, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, font=HEI)
    shade(mt.rows[0].cells[i], "DCE6F1")
for ri, row in enumerate(members, start=1):
    for ci, val in enumerate(row):
        set_cell(mt.rows[ri].cells[ci], val, align=WD_ALIGN_PARAGRAPH.CENTER if ci in (0,1,2) else WD_ALIGN_PARAGRAPH.LEFT)
para("（成绩栏由教师填写；学号 / 姓名请按实际小组成员补全）", size=8.5, color=RGBColor(0x88,0x88,0x88), space_before=2, space_after=6)

# ============ 考核要求 ============
heading("考核要求", size=12)
para("期末考试成绩构成：项目演示（30分） ＋ 课程总结（20分） ＋ 项目答辩（50分）", size=10, space_after=2)
for t in [
    "1. 项目演示：项目效果运行无误，功能完善，设计代码逻辑清晰、可读性好。",
    "2. 课程总结：PPT 设计简洁，内容结构合理，项目设计与实现过程阐述清晰，课程知识应用总结全面、正确。",
    "3. 项目答辩：每人回答 2-3 个问题，包括课程理论知识、项目实施中用到的知识等。",
]:
    para(t, size=10, space_after=2)

# ============ 答辩记录 ============
heading("答辩记录", size=12)

heading("一、项目演示", size=11)
para("1. 项目运行效果及功能说明", size=10, bold=True, space_before=4)
para("（每位组员上传个人负责模块的运行截图，并文字说明模块功能、实现逻辑、核心作用等；以下按六位成员分工逐项说明）",
     size=9, color=RGBColor(0x88,0x88,0x88), space_after=6)

modules = [
    ("图1", "【姓名1】· 用户与权限模块",
     "功能：用户注册、登录、注销，基于 HttpSession 维持登录态，并按用户表 role 字段区分普通用户与管理员角色。\n"
     "实现逻辑：LoginServlet 校验账号密码后写入 Session；AuthFilter 拦截需登录的请求，未登录跳转登录页；RegisterServlet 完成注册入库。\n"
     "核心作用：保障系统安全，隔离前台发帖与后台管理权限。"),
    ("图2", "【姓名2】· 板块与导航模块",
     "功能：首页展示三个板块（综合讨论 / 技术问答 / 资源分享），配 SVG 线性图标、浮动胶囊导航与全屏 Hero 头图。\n"
     "实现逻辑：BoardService.listAll() 查询板块；board-icon.jsp 按板块名映射 SVG 图标；header.jsp 提供全局头部、搜索与悬浮按钮。\n"
     "核心作用：提供清晰的内容分类与一致的全站导航体验。"),
    ("图3", "【姓名3】· 帖子模块",
     "功能：帖子列表（排序 / 搜索 / 分页）、帖子详情（含回复）、发帖、点赞与浏览量统计。\n"
     "实现逻辑：PostListServlet 处理筛选与分页；PostDao 通过 JOIN 取作者名；点赞与浏览量用 UPDATE 自增；搜索采用 LIKE 模糊匹配。\n"
     "核心作用：论坛核心内容载体，承载用户生产与消费的主要流量。"),
    ("图4", "【姓名4】· 回复模块",
     "功能：在帖子下发表回复，后台可对全部回复统一管理（删除不当内容）。\n"
     "实现逻辑：ReplyServlet 将内容写入 t_reply；ReplyDao 联表取出所属帖子 / 板块 / 作者；AdminServlet 提供回复列表与删除。\n"
     "核心作用：支撑讨论互动，并便于内容治理。"),
    ("图5", "【姓名5】· 后台管理模块",
     "功能：数据概览（用户 / 帖子 / 回复 / 板块数及今日新帖）、板块管理（增删改，删除时帖子可归并）、用户与内容管理。\n"
     "实现逻辑：AdminServlet 聚合统计；BoardService 维护板块；仅管理员可进入（AuthFilter 校验 role）。\n"
     "核心作用：系统运维与内容审核中枢。"),
    ("图6", "【姓名6】· AI 助手模块",
     "功能：DeepSeek 智能问答，以及发帖 / 回复的「✨ AI 帮写」草稿生成。\n"
     "实现逻辑：AiServlet 调用 DeepSeekUtil（JDK 原生 HttpURLConnection 直连 api.deepseek.com，零第三方依赖），按 kind=post / reply 拼装提示词返回草稿回填表单。\n"
     "核心作用：降低内容创作门槛，体现 AI 与 Java Web 的工程集成。"),
]

for tag, title, desc in modules:
    para(tag + "  " + title, size=10.5, bold=True, space_before=6, space_after=2)
    # 截图占位框（1x1 带边框表格）
    shot = doc.add_table(rows=1, cols=1); borders(shot, sz=6, color="999999")
    shot.rows[0].cells[0].text = ""
    pp = shot.rows[0].cells[0].paragraphs[0]; pp.alignment = WD_ALIGN_PARAGRAPH.CENTER
    rr = pp.add_run("（请在此粘贴「" + title.split('·')[1].strip() + "」运行截图）")
    rr.font.size = Pt(9); rr.font.color.rgb = RGBColor(0x99,0x99,0x99); set_cn(rr)
    # 单元格固定高度
    tc = shot.rows[0].cells[0]._tc.get_or_add_tcPr()
    trPr = tc.get_or_add_trPr() if False else None
    h = OxmlElement('w:tcH'); h.set(qn('w:val'),'exact'); h.set(qn('w:h'),'2600'); tc.append(h)
    para(desc, size=10, space_before=3, space_after=4, line=1.25)

# ============ 课程总结 ============
heading("二、课程总结", size=11)
heading("1. 项目设计与实现过程", size=10.5)
for t in [
    "需求分析：面向课程考核，构建一个轻量级在线论坛，支持用户注册登录、板块分类、发帖回帖、点赞搜索与后台管理，并集成 AI 辅助创作。",
    "总体设计：采用 MVC 分层——Model（JavaBean + DAO 封装数据与持久化）、View（JSP + EL/JSTL 渲染）、Controller（Servlet 调度请求），由 Filter 统一处理编码与登录校验、Listener 在应用启动时初始化种子数据。",
    "功能实现：六位成员按模块分工并行开发，使用 JDBC（sqlite-jdbc）手写 CRUD，前端以 JSP/EL/JSTL 完成动态展示，关键交互（点赞、搜索、AI 帮写）经 Servlet 后用 JSON/表单返回。",
    "测试与部署：本地 JDK8 + Tomcat8.5 编译联调，部署至云服务器 Tomcat，验证首页、发帖、后台与 AI 接口均正常运行。",
]:
    para("· " + t, size=10, space_after=2, line=1.25)

heading("2. 课程知识应用总结", size=10.5)
know = [
    ("Servlet / JSP", "Servlet 处理 HTTP 请求与转发，JSP 负责视图渲染，构成 MVC 的 C 与 V。"),
    ("EL / JSTL", "页面通过 ${} 取值、<c:forEach> 遍历帖子列表、<c:if> 做条件渲染，简化脚本。"),
    ("JavaBean", "User / Post / Reply / Board 等实体封装数据，贯穿各层传递。"),
    ("JDBC", "借助 sqlite-jdbc 建立连接、预编译语句完成增删改查与联表查询。"),
    ("MVC 架构", "控制、视图、模型分离，降低耦合，便于六人按模块并行协作。"),
    ("Filter / Listener", "EncodingFilter 统一 UTF-8 与缓存头；AppListener 启动时建表并写入板块种子数据。"),
    ("AI 集成（拓展）", "用 JDK 原生 HttpURLConnection 调用 DeepSeek REST API，手写 JSON 拼装与解析，零第三方依赖。"),
]
kt = doc.add_table(rows=len(know)+1, cols=2); borders(kt); kt.alignment = WD_TABLE_ALIGNMENT.CENTER
set_cell(kt.rows[0].cells[0], "知识点", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, font=HEI); shade(kt.rows[0].cells[0],"DCE6F1")
set_cell(kt.rows[0].cells[1], "在九组论坛中的应用", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, font=HEI); shade(kt.rows[0].cells[1],"DCE6F1")
for i,(k,v) in enumerate(know, start=1):
    set_cell(kt.rows[i].cells[0], k, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_cell(kt.rows[i].cells[1], v)
para("（此部分内容按课程要求手写亦可，此处已给出电子版要点）", size=8.5, color=RGBColor(0x88,0x88,0x88), space_before=2, space_after=6)

# ============ 项目答辩（示例问答，可手抄） ============
heading("三、项目答辩（参考问答）", size=11)
qa = [
    ("Q1：请说明本项目 MVC 各层的职责。",
     "A：Model（JavaBean + DAO）负责数据与持久化；View（JSP + EL/JSTL）负责页面展示；Controller（Servlet）负责接收请求、调用业务、转发视图。三层解耦，便于分工维护。"),
    ("Q2：接入 DeepSeek 为什么用原生 HttpURLConnection 而不引入 SDK？",
     "A：课程考核要求贴合 Servlet/JSP/JavaBean/JDBC 技术栈，原生调用 JDK 自带、零第三方依赖，且能清晰展示 HTTP 请求、JSON 拼装与流解析的全过程，更符合题意。"),
]
for q,a in qa:
    para(q, size=10, bold=True, space_before=4, space_after=1)
    para(a, size=10, space_after=4, line=1.25)

# ============ 页脚页码 ============
section = doc.sections[0]
footer = section.footer
fp = footer.paragraphs[0]; fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
run = fp.add_run()
fld1 = OxmlElement('w:fldSimple'); fld1.set(qn('w:instr'),'PAGE')
run._r.append(fld1)
run.font.size = Pt(9); set_cn(run)

out = r"C:/Users/ac010/WorkBuddy/2026-10-01-14-20-56/jiuzu-forum/九组论坛-JavaWeb期末考试报告单.docx"
doc.save(out)
print("SAVED:", out)
