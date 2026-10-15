# -*- coding: utf-8 -*-
"""本地冒烟测试：注册 -> 发带图帖子 -> 带图回复 -> 详情页校验图片渲染"""
import urllib.request, urllib.parse, urllib.error, http.cookiejar, os, re, zlib, struct, random, string

BASE = "http://localhost:8080/jiuzu-forum"
PW = "Aa123456"
USER = "smokeimg" + str(random.randint(1000, 9999))

# 禁用代理（本机直连）
opener_no_proxy = urllib.request.build_opener(urllib.request.ProxyHandler({}))


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def make_png(w=6, h=6, rgb=(0, 180, 216)):
    def chunk(t, d):
        c = t + d
        return struct.pack('>I', len(d)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
    raw = b''
    for _ in range(h):
        raw += b'\x00' + bytes(rgb) * w
    return (b'\x89PNG\r\n\x1a\n'
            + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(raw))
            + chunk(b'IEND', b''))


def post_form(url, fields, cj):
    """普通 urlencoded 表单（注册/登录用），返回 (code, location, body)"""
    op = urllib.request.build_opener(
        urllib.request.ProxyHandler({}),
        urllib.request.HTTPCookieProcessor(cj),
        NoRedirect())
    data = urllib.parse.urlencode(fields).encode('utf-8')
    req = urllib.request.Request(url, data, {
        'User-Agent': 'Mozilla/5.0',
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'})
    try:
        r = op.open(req, timeout=30)
        return r.status, r.headers.get('Location', ''), r.read().decode('utf-8', 'ignore')
    except urllib.error.HTTPError as e:
        return e.code, e.headers.get('Location', ''), e.read().decode('utf-8', 'ignore')


def post(url, fields, files=None, cj=None):
    """提交 multipart/form-data，返回 (code, location, body)"""
    cj = cj or http.cookiejar.CookieJar()
    handlers = [urllib.request.HTTPCookieProcessor(cj), NoRedirect()]
    handlers.insert(0, urllib.request.ProxyHandler({}))
    op = urllib.request.build_opener(*handlers)
    boundary = '----Smoke' + ''.join(random.choices(string.ascii_letters + string.digits, k=16))
    body = b''
    for k, v in fields.items():
        body += ('--%s\r\nContent-Disposition: form-data; name="%s"\r\n\r\n' % (boundary, k)).encode()
        body += str(v).encode('utf-8') + b'\r\n'
    for k, (fn, data, ct) in (files or {}).items():
        body += ('--%s\r\nContent-Disposition: form-data; name="%s"; filename="%s"\r\n'
                 'Content-Type: %s\r\n\r\n' % (boundary, k, fn, ct)).encode()
        body += data + b'\r\n'
    body += ('--%s--\r\n' % boundary).encode()
    req = urllib.request.Request(url, body, {
        'User-Agent': 'Mozilla/5.0',
        'Content-Type': 'multipart/form-data; boundary=' + boundary,
    })
    try:
        r = op.open(req, timeout=30)
        return r.status, r.headers.get('Location', ''), r.read().decode('utf-8', 'ignore')
    except urllib.error.HTTPError as e:
        return e.code, e.headers.get('Location', ''), e.read().decode('utf-8', 'ignore')


def get(url):
    try:
        r = opener_no_proxy.open(url, timeout=20)
        return r.status, r.read().decode('utf-8', 'ignore')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8', 'ignore')


png = make_png()
print('[0] PNG 样例 %d 字节' % len(png))

cj = http.cookiejar.CookieJar()

# 1) 注册（普通表单）
c, loc, body = post_form(BASE + '/register', {
    'username': USER, 'password': PW, 'confirm': PW, 'nickname': '冒烟测试'}, cj)
print('[1] 注册 %s -> %s  loc=%s' % (USER, c, loc))
if c != 302:
    err = re.search(r'class="alert error">([^<]+)', body)
    print('    !! 注册失败，页面提示：%s' % (err.group(1) if err else '未知'))
    raise SystemExit(1)

# 2) 发带图帖子
title = '冒烟测试帖' + str(random.randint(100, 999))
c, loc, _ = post(BASE + '/post/add', {
    'boardId': '1', 'title': title, 'content': '这是一条带图片的冒烟测试帖子正文。'},
    files={'image': ('test.png', png, 'image/png')}, cj=cj)
print('[2] 发帖 -> %s  loc=%s' % (c, loc))

# 3) 找到新帖 ID（列表置顶帖排前面，逐个详情页确认标题）
status, html = get(BASE + '/posts?sort=new')
cands = []
for m in re.finditer(r'/post/detail\?id=(\d+)', html):
    if m.group(1) not in cands:
        cands.append(m.group(1))
new_id = None
for pid in cands:
    _, dh = get(BASE + '/post/detail?id=%s' % pid)
    if title in dh:
        new_id = pid
        break
print('[3] 按标题定位到新帖 id=%s（候选 %s）' % (new_id, cands))
if not new_id:
    print('!! 找不到新帖'); raise SystemExit(1)

# 4) 带图回复
c, loc, _ = post(BASE + '/reply/add', {
    'postId': new_id, 'content': '这条回复带一张图，看看能不能显示。'},
    files={'image': ('reply.png', png, 'image/png')}, cj=cj)
print('[4] 回复 -> %s  loc=%s' % (c, loc))

# 5) 详情页校验
status, html = get(BASE + '/post/detail?id=%s' % new_id)
post_imgs = re.findall(r'/uploads/posts/[^"\']+', html)
reply_imgs = re.findall(r'/uploads/replies/[^"\']+', html)
print('[5] 详情页 HTTP %s' % status)
print('    帖子配图 %d 个: %s' % (len(post_imgs), post_imgs[:1]))
print('    回复配图 %d 个: %s' % (len(reply_imgs), reply_imgs[:1]))

# 6) 图片能否真实取到（200 + PNG 魔数）
ok = 0
for p in (post_imgs[:1] + reply_imgs[:1]):
    url = BASE + p.replace(BASE, '') if p.startswith('http') else BASE + p
    try:
        r = opener_no_proxy.open(url, timeout=10)
        data = r.read()
        if r.status == 200 and data[:8] == b'\x89PNG\r\n\x1a\n':
            ok += 1
            print('    [OK] 图片可访问 %s (%d 字节, PNG)' % (p, len(data)))
        else:
            print('    [!!] 图片异常 %s -> %s' % (p, r.status))
    except Exception as e:
        print('    [!!] 图片取不到 %s -> %s' % (p, e))

# 7) 仅图片（无正文）的回复
c, loc, _ = post(BASE + '/reply/add', {'postId': new_id, 'content': ''},
                 files={'image': ('only.png', png, 'image/png')}, cj=cj)
print('[6] 仅图片回复 -> %s  loc=%s' % (c, loc))

# 8) 非法类型应被拒
c, loc, body = post(BASE + '/reply/add', {'postId': new_id, 'content': '非法测试'},
                    files={'image': ('bad.txt', b'hello', 'text/plain')}, cj=cj)
print('[7] 非法图片类型 -> %s  loc=%s (应 302 回详情页，带 flashError)' % (c, loc))

status, html = get(BASE + '/post/detail?id=%s' % new_id)
print('    当前回复配图总数: %d' % len(re.findall(r'/uploads/replies/[^"\']+', html)))
print('\n=== 冒烟测试结束，图片可访问数 %d/2 ===' % ok)
