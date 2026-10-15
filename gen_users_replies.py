# -*- coding: utf-8 -*-
import urllib.request, urllib.parse, urllib.error, http.cookiejar, os

BASE = "http://129.211.29.190/jiuzu-forum"
POST_ID = "3"
PW = "Aa123456"

# (用户名[latin,4-16], 昵称[中文], 评论标签, 评论内容)
data = [
    ("yishao_wanfeng",   "一勺晚风",         "扣1", "扣1！朱思成这颜值我服，班里第一没悬念。"),
    ("code_wuyanzu",     "代码界的吴彦祖",   "扣1", "扣1，上次篮球赛他投篮那个侧脸绝了，谁懂啊。"),
    ("xiao_zhang204",    "隔壁班的小张",     "扣1", "扣1+1，不仅帅还温柔，公认的校草没跑。"),
    ("fangdu_gui",       "深夜放毒专家",     "扣1", "扣1，这还用问？明明是大家默认的高颜值好吧。"),
    ("lemon_jing",       "柠檬精本精",       "扣1", "扣1，但我怀疑评论区有人开了十级滤镜（狗头）。"),
    ("tuitang_gu",       "退堂鼓表演艺术家", "扣1", "扣1，朱思成我偶像（不是），颜值和性格都在线。"),
    ("chigua_jun",       "路过的吃瓜群众",   "扣2", "扣2，审美因人而异，我觉得也就一般般。"),
    ("coder_aqiang",     "程序猿阿强",       "扣2", "扣2，帅是有标准的，他顶多算清秀挂。"),
    ("sanyue_cat",       "三月鱼的猫",       "扣2", "扣2，你们滤镜太厚了，我站隔壁班那位。"),
    ("xuewei_lazy",      "不务正业的学委",   "扣2", "扣2，比上不足比下有余吧，别神化。"),
    ("milk_tea99",       "奶茶续命选手",     "扣2", "扣2，先问过朱思成本人同意没（手动狗头）。"),
    ("buxi_zuoye",       "今天也不想写作业", "扣2", "扣2，平平无奇但我也不否认他顺眼，各退一步。"),
]

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None

proxy = os.environ.get("http_proxy") or os.environ.get("HTTP_PROXY")
def make_opener(cj):
    h = []
    if proxy: h.append(urllib.request.ProxyHandler({"http": proxy, "https": proxy}))
    h.append(urllib.request.HTTPCookieProcessor(cj))
    h.append(NoRedirect())
    return urllib.request.build_opener(*h)

def req(url, body=None):
    if body is None:
        return urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    return urllib.request.Request(url, body,
            headers={"User-Agent": "Mozilla/5.0",
                     "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"})

def call(op, url, body=None):
    """返回 (code, location)。NoRedirect 下 302 会抛 HTTPError，从中取 header。"""
    try:
        r = op.open(req(url, body), timeout=20)
        return r.status, r.headers.get("Location", "")
    except urllib.error.HTTPError as e:
        return e.code, e.headers.get("Location", "")

ok_u = ok_r = 0
for i, (uname, nick, tag, content) in enumerate(data, 1):
    cj = http.cookiejar.CookieJar()
    op = make_opener(cj)
    # 1) 注册（成功=302 到 /posts?welcome=1；失败=200 停留登录页）
    code, loc = call(op, f"{BASE}/register",
                urllib.parse.urlencode({"username": uname, "password": PW,
                                        "confirm": PW, "nickname": nick}).encode("utf-8"))
    reg_ok = (code == 302 and "welcome=1" in loc)
    if reg_ok: ok_u += 1
    # 2) 发评论（成功=302 到 /post/detail?id=3；未登录则 302 到 /login）
    code2, loc2 = call(op, f"{BASE}/reply/add",
                urllib.parse.urlencode({"postId": POST_ID, "content": content}).encode("utf-8"))
    rep_ok = (code2 == 302 and f"id={POST_ID}" in loc2)
    if rep_ok: ok_r += 1
    print(f"[{i:2}] {uname:<14} 注册{'OK' if reg_ok else 'FAIL('+str(code)+':'+loc+')'} | "
          f"评论[{tag}]{'OK' if rep_ok else 'FAIL('+str(code2)+':'+loc2+')'}")

print(f"\n=== 汇总：注册 {ok_u}/{len(data)}，评论 {ok_r}/{len(data)} ===")
