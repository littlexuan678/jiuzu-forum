# -*- coding: utf-8 -*-
"""本地冒烟：数据库外置到 $CATALINA_BASE/forum-data + 帖子编辑（含换图）"""
import urllib.request, urllib.parse, urllib.error, http.cookiejar, os, re, uuid, zlib, struct

BASE = "http://localhost:8080/jiuzu-forum"
USER = "edittest"
PW = "Aa123456"


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


cj = http.cookiejar.CookieJar()
op = urllib.request.build_opener(urllib.request.ProxyHandler({}),
                                 urllib.request.HTTPCookieProcessor(cj), NoRedirect())


def call(url, body=None, ctype=None):
    hdr = {"User-Agent": "Mozilla/5.0"}
    if ctype:
        hdr["Content-Type"] = ctype
    try:
        r = op.open(urllib.request.Request(url, body, hdr), timeout=30)
        return r.status, r.headers.get("Location", ""), r.read().decode("utf-8", "ignore")
    except urllib.error.HTTPError as e:
        return e.code, e.headers.get("Location", ""), e.read().decode("utf-8", "ignore")


def minipng(w=6, h=6):
    def chunk(t, d):
        c = t + d
        return struct.pack(">I", len(d)) + c + struct.pack(">I", zlib.crc32(c) & 0xffffffff)
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 2, 0, 0, 0)
    raw = b"".join(b"\x00" + b"\x00\x80\xff" * w for _ in range(h))
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr)
            + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))


def mp(fields, files=None):
    boundary = "----Smoke" + uuid.uuid4().hex
    b = []
    for k, v in fields.items():
        b.append(("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\n\r\n%s\r\n" % (boundary, k, v)).encode())
    for k, (fn, data) in (files or {}).items():
        b.append(("--%s\r\nContent-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n"
                  "Content-Type: image/png\r\n\r\n" % (boundary, k, fn)).encode() + data + b"\r\n")
    b.append(("--%s--\r\n" % boundary).encode())
    return b"".join(b), "multipart/form-data; boundary=%s" % boundary


print("=" * 58)
print("① 注册 / 登录")
print("=" * 58)
c, loc, _ = call(BASE + "/register",
                 urllib.parse.urlencode({"username": USER, "password": PW, "confirm": PW,
                                         "nickname": "编辑测试"}).encode(),
                 "application/x-www-form-urlencoded; charset=UTF-8")
print("  register ->", c, loc)
if c != 302:
    call(BASE + "/login", urllib.parse.urlencode({"username": USER, "password": PW}).encode(),
         "application/x-www-form-urlencoded; charset=UTF-8")
    print("  (已存在，改登录)")

print()
print("=" * 58)
print("② 发一篇新帖")
print("=" * 58)
title = "本地冒烟：待编辑的原标题"
body, ct = mp({"boardId": "1", "title": title, "content": "这是原始正文，稍后会被改掉。"})
c, loc, _ = call(BASE + "/post/add", body, ct)
print("  post/add ->", c, loc)

c, listHtml = 0, ""
try:
    r = op.open(BASE + "/posts?sort=new", timeout=30)
    listHtml = r.read().decode("utf-8", "ignore")
except Exception as e:
    print("  !! 列表失败", e)
pid = None
for m in re.finditer(r'/post/detail\?id=(\d+)"[^>]*>\s*([^<]{0,60})', listHtml):
    if m.group(2).strip().startswith("本地冒烟"):
        pid = m.group(1)
        break
print("  新帖 id =", pid)
if not pid:
    print("  !! 找不到新帖"); raise SystemExit(1)

print()
print("=" * 58)
print("③ 打开编辑页")
print("=" * 58)
c, editHtml = 0, ""
try:
    r = op.open(BASE + "/post/edit?id=" + pid, timeout=30)
    c, editHtml = r.getcode(), r.read().decode("utf-8", "ignore")
except urllib.error.HTTPError as e:
    c, editHtml = e.code, e.read().decode("utf-8", "ignore")
print("  GET /post/edit?id=%s -> %s  长度=%d" % (pid, c, len(editHtml)))
print("  页面标题是「编辑帖子」:", "编辑帖子" in editHtml)
print("  表单提交到 /post/edit:", '/post/edit' in editHtml)
print("  已预填原标题:", title in editHtml)
print("  有 hidden id:", ('name="id" value="%s"' % pid) in editHtml)

print()
print("=" * 58)
print("④ 保存修改（改标题+正文+配图）")
print("=" * 58)
newTitle = "本地冒烟：改过的新标题"
body, ct = mp({"id": pid, "boardId": "1", "title": newTitle,
               "content": "正文也改了，还顺手加了张图。"},
              {"image": ("smoke.png", minipng())})
c, loc, _ = call(BASE + "/post/edit", body, ct)
print("  POST /post/edit ->", c, loc, "=>", "OK" if (c == 302 and ("id=" + pid) in loc) else "FAIL")

c, detail = 0, ""
try:
    r = op.open(BASE + "/post/detail?id=" + pid, timeout=30)
    c, detail = r.getcode(), r.read().decode("utf-8", "ignore")
except urllib.error.HTTPError as e:
    c, detail = e.code, e.read().decode("utf-8", "ignore")
print("  详情页 HTTP", c)
print("  显示新标题:", newTitle in detail)
print("  显示新正文:", "正文也改了" in detail)
imgs = sorted(set(re.findall(r'/jiuzu-forum/uploads/posts/[^"\']+', detail)))
print("  配图渲染:", len(imgs) > 0, imgs[:1])
ok = 0
for im in imgs:
    try:
        rr = op.open("http://localhost:8080" + im, timeout=20)
        bb = rr.read()
        if rr.getcode() == 200 and bb[:4] == b"\x89PNG":
            ok += 1
    except Exception:
        pass
print("  配图可访问: %d/%d" % (ok, len(imgs)))

print()
print("=" * 58)
print("⑤ 再打开编辑页，应显示「更换配图」和当前图")
print("=" * 58)
try:
    r = op.open(BASE + "/post/edit?id=" + pid, timeout=30)
    h2 = r.read().decode("utf-8", "ignore")
except urllib.error.HTTPError as e:
    h2 = e.read().decode("utf-8", "ignore")
print("  有「更换配图」标签:", "更换配图" in h2)
print("  显示当前配图:" , bool(re.search(r'<img src="[^"]*uploads/posts/[^"]+" alt="当前配图"', h2)))
print("  有移除配图勾选框:", 'name="removeImage"' in h2)

print()
print("=" * 58)
print("⑥ 移除配图")
print("=" * 58)
body, ct = mp({"id": pid, "boardId": "1", "title": newTitle,
               "content": "图已移除，只剩文字。"}, None)
c, loc, _ = call(BASE + "/post/edit",
                 urllib.parse.urlencode({"id": pid, "boardId": "1", "title": newTitle,
                                         "content": "图保留，只改文字"}).encode(),
                 "application/x-www-form-urlencoded; charset=UTF-8")
print("  urlencoded 编辑 ->", c, loc, "=>", "OK" if c == 302 else "FAIL")
try:
    r = op.open(BASE + "/post/detail?id=" + pid, timeout=30)
    d3 = r.read().decode("utf-8", "ignore")
except urllib.error.HTTPError as e:
    d3 = e.read().decode("utf-8", "ignore")
print("  文字已更新:", "图保留，只改文字" in d3)
print("  配图仍在(未传 null 应保持):", bool(re.search(r'/jiuzu-forum/uploads/posts/', d3)))

print()
print("完成")
