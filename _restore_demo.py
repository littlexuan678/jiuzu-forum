# -*- coding: utf-8 -*-
"""重建演示数据：3 篇帖子 + 12 个真人风格用户 + 扣1/扣2 评论
（旧的论坛数据库被部署时误删，这里把演示内容重新灌回去）"""
import urllib.request, urllib.parse, urllib.error, http.cookiejar, os, re, time

BASE = "http://129.211.29.190/jiuzu-forum"
PW = "Aa123456"
POLL_TITLE = "朱思成是不是大帅哥？进来扣1还是扣2"

POSTS = [
    (1, "从需求到上线：我们九组论坛的诞生记",
     """做这个论坛之前我们对 Java Web 的理解基本停留在课本：Servlet 怎么写、JSP 怎么取值。
真上手才发现，难点从来不是某一个 API，而是怎么把东西摆顺。

一开始我们争论要不要上框架。后来想明白了：课程设计要的是把底层跑通——
请求怎么进来、数据怎么存、页面怎么渲染，这些如果交给框架代劳，学的东西就剩个注解怎么写。
于是定下来：原生 Servlet + JSP + JSTL/EL + JavaBean + JDBC，老老实实手写一遍 MVC。

分层是被逼出来的。最早所有逻辑都堆在 Servlet 里，一个 doPost 两百行，
改一个字段要翻半天。后来拆成 bean / dao / service / web 四层，
DAO 只管 SQL，Service 管业务规则和权限判定，Servlet 只做取参、调用、跳转。
拆完之后加一个新功能就是往 Service 里加一个方法，心智负担小太多了。

踩过的坑也值得记一笔：
1. JSP 模板里出现井号加花括号会被当成 EL 表达式，整个页面直接 500，连注释里都不行。
2. Tomcat 调 getPart 必须在 web.xml 配 multipart-config，否则上传功能一调就 500。
3. 数据库文件如果放在 webapps 目录里，重新部署就会被一起删掉——现在挪到 catalina.base 下面了。

最后说一句：能在线上跑起来、能被同班同学真的拿去发帖回帖，比分数更有实感。"""),
    (2, "聊聊 Filter 和 Listener：那些不显眼但很关键的组件",
     """这俩东西平时没什么存在感，但真少了它们系统会立刻散架。

Filter 我们用在三个地方：
- 统一编码：以前每个 Servlet 开头都要写那三行 setCharacterEncoding，现在一个 Filter 全拦下来。
- 登录校验：AuthFilter 白名单放行 login/register/静态资源，其余路径没会话就踢回登录页，
  并且带上 redirect 参数，登录完还能回到原来想去的页面。
- 后台权限：所有 /admin/* 的请求统一判定 admin 标记，不用在每个 Servlet 里重复写判断。

Listener 我们只用了一个，但很重要：AppListener 在 Web 应用启动时拿 ServletContext 的真实路径
去初始化数据库——建表、跑迁移、灌初始数据。如果这件事放在某个 Servlet 的 init 里，
用户第一次访问哪个页面就成了薛定谔的问题。

顺便说几个当时绕了很久的点：
1. 过滤器的 url-pattern 配 /* 会连 CSS、JS 一起拦，得先把静态资源放行，不然页面裸奔。
2. 已上线库里加列不能靠 CREATE TABLE IF NOT EXISTS，它压根不会补列，
   必须写成 ALTER TABLE 加 try/catch 忽略重复列的迁移逻辑。
3. SQLite 是单文件嵌入式，读写同一份文件，部署到服务器时权限一定要给对，
   否则应用启动时报数据库初始化失败，而且日志还写不进去，非常难查。

结论：Filter 管横向切入的关注点，Listener 管生命周期钩子。写好了它们就等于给系统加了层免疫系统。"""),
    (1, POLL_TITLE,
     """先声明：不搞人身攻击，就是个投票帖。

扣1 = 承认，确实帅
扣2 = 保留意见，觉得一般

评论区规矩：
- 允许开玩笑，但别上升到人身攻击
- 别刷屏，一人扣一次就行
- 欢迎带论据，比如\"篮球赛那个侧脸\"这种具体场景

我先扣1，剩下的交给评论区。"""),
]

USERS = [
    ("yishao_wanfeng", "一勺晚风", "扣1", "扣1！朱思成这颜值我服，班里第一没悬念。"),
    ("code_wuyanzu", "代码界的吴彦祖", "扣1", "扣1，上次篮球赛他投篮那个侧脸绝了，谁懂啊。"),
    ("xiao_zhang204", "隔壁班的小张", "扣1", "扣1+1，不仅帅还温柔，公认的校草没跑。"),
    ("fangdu_gui", "深夜放毒专家", "扣1", "扣1，这还用问？明明是大家默认的高颜值好吧。"),
    ("lemon_jing", "柠檬精本精", "扣1", "扣1，但我怀疑评论区有人开了十级滤镜（狗头）。"),
    ("tuitang_gu", "退堂鼓表演艺术家", "扣1", "扣1，朱思成我偶像（不是），颜值和性格都在线。"),
    ("chigua_jun", "路过的吃瓜群众", "扣2", "扣2，审美因人而异，我觉得也就一般般。"),
    ("coder_aqiang", "程序猿阿强", "扣2", "扣2，帅是有标准的，他顶多算清秀挂。"),
    ("sanyue_cat", "三月鱼的猫", "扣2", "扣2，你们滤镜太厚了，我站隔壁班那位。"),
    ("xuewei_lazy", "不务正业的学委", "扣2", "扣2，比上不足比下有余吧，别神化。"),
    ("milk_tea99", "奶茶续命选手", "扣2", "扣2，先问过朱思成本人同意没（手动狗头）。"),
    ("buxi_zuoye", "今天也不想写作业", "扣2", "扣2，平平无奇但我也不否认他顺眼，各退一步。"),
]


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


proxy = os.environ.get("http_proxy") or os.environ.get("HTTP_PROXY")


def make_opener(cj):
    h = []
    if proxy:
        h.append(urllib.request.ProxyHandler({"http": proxy, "https": proxy}))
    h += [urllib.request.HTTPCookieProcessor(cj), NoRedirect()]
    return urllib.request.build_opener(*h)


FORM = {"User-Agent": "Mozilla/5.0",
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"}


def call(op, url, body=None):
    req = urllib.request.Request(url, body, FORM) if body is not None else \
        urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    for attempt in range(4):
        try:
            r = op.open(req, timeout=25)
            return r.status, r.headers.get("Location", "")
        except urllib.error.HTTPError as e:
            return e.code, e.headers.get("Location", "")
        except Exception:
            time.sleep(2)
    return "ERR", ""


print("① 管理员登录")
cj = http.cookiejar.CookieJar()
op = make_opener(cj)
c, loc = call(op, BASE + "/login",
              urllib.parse.urlencode({"username": "admin", "password": "admin123"}).encode())
print("   login ->", c, loc)
if c != 302:
    print("   !! 登录失败，退出"); raise SystemExit(1)

print()
print("② 重建帖子")
new_ids = []
for boardId, title, content in POSTS:
    c, loc = call(op, BASE + "/post/add",
                  urllib.parse.urlencode({"boardId": boardId, "title": title,
                                          "content": content}).encode())
    print("   [%s] -> %s" % (title[:22], c))
    time.sleep(0.6)

print()
print("③ 找投票帖 ID")
r = op.open(BASE + "/posts?sort=new", timeout=25)
html = r.read().decode("utf-8", "ignore")
poll_id = None
for m in re.finditer(r'/post/detail\?id=(\d+)"[^>]*>\s*([^<]{0,60})', html):
    if "朱思成" in m.group(2):
        poll_id = m.group(1)
        break
if not poll_id:
    # 列表页标题可能被截断，退而用关键词搜
    for m in re.finditer(r'/post/detail\?id=(\d+)', html):
        pass
    poll_id = None
print("   投票帖 id =", poll_id)
if not poll_id:
    print("   !! 找不到投票帖"); raise SystemExit(1)

print()
print("④ 生成 12 个用户 + 评论")
ok_u = ok_r = 0
for i, (uname, nick, tag, content) in enumerate(USERS, 1):
    cj2 = http.cookiejar.CookieJar()
    op2 = make_opener(cj2)
    c1, l1 = call(op2, BASE + "/register",
                  urllib.parse.urlencode({"username": uname, "password": PW,
                                          "confirm": PW, "nickname": nick}).encode())
    reg_ok = (c1 == 302 and "welcome=1" in l1)
    if not reg_ok:
        c1, l1 = call(op2, BASE + "/login",
                      urllib.parse.urlencode({"username": uname, "password": PW}).encode())
        reg_ok = (c1 == 302)
    if reg_ok:
        ok_u += 1
    c2, l2 = call(op2, BASE + "/reply/add",
                  urllib.parse.urlencode({"postId": poll_id, "content": content}).encode())
    rep_ok = (c2 == 302 and ("id=" + poll_id) in l2)
    if not rep_ok:
        c2, l2 = call(op2, BASE + "/reply/add",
                      urllib.parse.urlencode({"postId": poll_id, "content": content}).encode())
        rep_ok = (c2 == 302 and ("id=" + poll_id) in l2)
    if rep_ok:
        ok_r += 1
    print("   [%2d] %-16s 账号%s 评论[%s]%s" %
          (i, nick, "OK" if reg_ok else "FAIL", tag, "OK" if rep_ok else "FAIL(" + str(c2) + ")"))
    time.sleep(0.4)

print()
print("=== 完成：账号 %d/%d，评论 %d/%d，投票帖 id=%s ===" % (ok_u, len(USERS), ok_r, len(USERS), poll_id))
