# -*- coding: utf-8 -*-
"""线上验证：页面可用性 + 图片上传链路真实测试（测试后自动删除）"""
import urllib.request, urllib.parse, urllib.error, http.cookiejar, os, re, uuid

BASE = "http://129.211.29.190/jiuzu-forum"
POST_ID = "3"
proxy = os.environ.get("http_proxy")


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


cj = http.cookiejar.CookieJar()
handlers = [urllib.request.ProxyHandler({"http": proxy, "https": proxy})] if proxy else []
handlers += [urllib.request.HTTPCookieProcessor(cj), NoRedirect()]
op = urllib.request.build_opener(*handlers)


def call(url, body=None, ctype=None):
    hdr = {"User-Agent": "Mozilla/5.0"}
    if ctype:
        hdr["Content-Type"] = ctype
    try:
        r = op.open(urllib.request.Request(url, body, hdr), timeout=30)
        return r.status, r.headers.get("Location", ""), r.read().decode("utf-8", "ignore")
    except urllib.error.HTTPError as e:
        return e.code, e.headers.get("Location", ""), e.read().decode("utf-8", "ignore")


def get(url):
    try:
        r = op.open(urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30)
        return r.getcode(), r.read()
    except urllib.error.HTTPError as e:
        return e.code, e.read()


def minipng():
    import zlib, struct
    def chunk(t, d):
        c = t + d
        return struct.pack(">I", len(d)) + c + struct.pack(">I", zlib.crc32(c) & 0xffffffff)
    ihdr = struct.pack(">IIBBBBB", 4, 4, 8, 2, 0, 0, 0)
    raw = b"".join(b"\x00" + b"\xff\x00\x00" * 4 for _ in range(4))
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr)
            + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))


print("=" * 60)
print("① 页面可用性")
print("=" * 60)
for path, label in [("/posts", "帖子列表"), ("/ai", "AI 助手页"), ("/login", "登录页"),
                    ("/static/css/style.v8.css", "CSS")]:
    code, body = get(BASE + path)
    print("  %-14s %s  %d 字节" % (label, code, len(body)))

code, AIHTML = get(BASE + "/ai")
ai = AIHTML.decode("utf-8", "ignore")
print("  AI 页新功能:")
for key, label in [("ai-suggest", "推荐 chip 区"), ("ai-write", "帮写面板"),
                   ("mdToHtml", "Markdown 渲染"), ("换个说法", "换个说法按钮"),
                   ("复制", "复制按钮")]:
    print("    %-16s %s" % (label, "✓" if key in ai else "✗"))
m = re.search(r"ai-status ([a-z]+)", ai)
print("    %-16s %s" % ("连接状态", m.group(1) if m else "?"))
print("    推荐问题数: %d" % len(re.findall(r'class="chip" data-q', ai)))

print()
print("=" * 60)
print("② 登录 + 带图回复")
print("=" * 60)
c, loc, _ = call(BASE + "/login",
                 urllib.parse.urlencode({"username": "admin", "password": "admin123"}).encode(),
                 "application/x-www-form-urlencoded; charset=UTF-8")
print("  登录 admin -> %s %s" % (c, loc))

boundary = "----VerifyBoundary" + uuid.uuid4().hex
png = minipng()


def mp(fields, files):
    b = []
    for k, v in fields.items():
        b.append(("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\n\r\n%s\r\n" % (boundary, k, v)).encode())
    for k, (fn, data) in files.items():
        b.append(("--%s\r\nContent-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n"
                  "Content-Type: image/png\r\n\r\n" % (boundary, k, fn)).encode() + data + b"\r\n")
    b.append(("--%s--\r\n" % boundary).encode())
    return b"".join(b)


body = mp({"postId": POST_ID, "content": "[自动验证] 带图回复，稍后删除"}, {"image": ("verify.png", png)})
c, loc, html = call(BASE + "/reply/add", body, "multipart/form-data; boundary=%s" % boundary)
print("  上传回复 -> %s  loc=%s" % (c, loc))
if c != 302 or ("id=" + POST_ID) not in loc:
    print("  !! 上传失败，退出"); raise SystemExit(1)

print()
print("=" * 60)
print("③ 详情页是否渲染出图片")
print("=" * 60)
code, detail = get(BASE + "/post/detail?id=" + POST_ID)
d = detail.decode("utf-8", "ignore")
imgs = re.findall(r'/jiuzu-forum/uploads/[a-zA-Z0-9_/\.\-]+', d)
imgs = sorted(set(imgs))
print("  详情页 200 = %s | 发现图片链接 %d 个" % (code == 200, len(imgs)))
ok = 0
for im in imgs[-3:]:
    c2, b2 = get("http://129.211.29.190" + im)
    good = (c2 == 200 and len(b2) > 0 and b2[:4] == b"\x89PNG")
    ok += 1 if good else 0
    print("    %s  HTTP %s  %d 字节  PNG头=%s" % (im, c2, len(b2), b2[:4] == b"\x89PNG"))
print("  图片可访问: %d/%d" % (ok, len(imgs[-3:])))
print("  详情页有 rc-media 样式: %s" % ("rc-media" in d))
print("  回复表单 multipart:    %s" % ("multipart/form-data" in d))

print()
print("=" * 60)
print("④ 清理测试数据")
print("=" * 60)
code, adm = get(BASE + "/admin/replies")
admh = adm.decode("utf-8", "ignore")
ids = re.findall(r'delReply\?id=(\d+)', admh)
if not ids:
    ids = re.findall(r'[?&]id=(\d+)[^"]*"[^>]*>删除', admh)
if ids:
    rid = max(int(x) for x in ids)
    c, loc, _ = call(BASE + "/admin/delReply?id=%d" % rid, None, None)
    print("  删除最新回复 id=%d -> %s %s" % (rid, c, loc))
    code, d2 = get(BASE + "/post/detail?id=" + POST_ID)
    left = d2.decode("utf-8", "ignore").count("[自动验证]")
    print("  剩余测试回复: %d" % left)
else:
    print("  !! 后台没解析到回复 id，需要手动删除 id 最大的那条")
print()
print("完成")
