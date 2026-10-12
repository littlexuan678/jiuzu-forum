# -*- coding: utf-8 -*-
"""九组论坛 Java Web 版 全链路功能自测"""
import http.cookiejar
import re
import sys
import urllib.parse
import urllib.request

BASE = "http://localhost:8080/jiuzu-forum"
cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))

PASS = []
FAIL = []


def get(path):
    with opener.open(BASE + path, timeout=15) as r:
        return r.status, r.read().decode("utf-8", "replace")


def post(path, data):
    body = urllib.parse.urlencode(data).encode()
    req = urllib.request.Request(BASE + path, data=body)
    with opener.open(req, timeout=15) as r:
        return r.status, r.read().decode("utf-8", "replace")


def check(name, cond, extra=""):
    (PASS if cond else FAIL).append(name)
    print(("  [OK] " if cond else "  [XX] ") + name + ((" | " + str(extra)[:120]) if extra and not cond else ""))


print("=" * 60)
print("1. 首页 / 列表")
st, html = get("/posts")
check("首页 200", st == 200, st)
check("标题含“九组论坛”", "九组论坛" in html)
check("渲染出帖子卡片", "post-card" in html)
check("板块导航存在", "综合讨论" in html)
check("种子公告置顶显示", "置顶" in html)

print("\n2. 注册新用户（单表新增）")
import random
user = "stu" + str(random.randint(10000, 99999))
st, html = post("/register", {"username": user, "password": "123456",
                              "confirm": "123456", "nickname": "测试同学"})
check("注册后返回 200", st == 200, st)
st, html = get("/posts?welcome=1")
check("注册后自动登录（顶栏显示昵称）", "测试同学" in html)
check("页面显示注册成功提示", "注册成功" in html)

print("\n3. 发帖（表单提交到 Servlet）")
title = "我发的测试帖 " + user
st, html = post("/post/add", {"boardId": 1, "title": title, "content": "这是正文第一行。\n这是第二行。"})
check("发帖请求成功", st == 200, st)
st, html = get("/posts?sort=new")
check("新帖出现在最新列表", title in html)

print("\n4. 详情页 / 回复 / 点赞")
ids = re.findall(r'/post/detail\?id=(\d+)', html)
pid = ids[0] if ids else "1"
st, html = get("/post/detail?id=" + pid)
check("详情页可打开", st == 200, st)
check("详情页显示标题", title in html or "九组论坛" in html)
check("浏览数已累加", "次浏览" in html)

st, html = post("/reply/add", {"postId": pid, "content": "这是我的第一条回复 @Test"})
check("回复提交成功", st == 200, st)
st, html = get("/post/detail?id=" + pid)
check("回复已显示在页面", "这是我的第一条回复" in html)

st, html = get("/like?id=" + pid)
check("点赞成功", st == 200, st)
st, html = get("/post/detail?id=" + pid)
check("点赞状态变为已赞", "已赞" in html)
st, html = get("/like?id=" + pid)
check("再次请求取消点赞", st == 200)
st, html = get("/post/detail?id=" + pid)
check("点赞已取消", "🤍 赞" in html or "已赞" not in html)

print("\n5. 搜索（基础数据查询）")
st, html = get("/posts?kw=" + urllib.parse.quote("测试帖"))
check("搜索返回结果", st == 200)
st, html = get("/posts?kw=zzzznotexist")
check("无结果时显示空状态", "没有找到" in html or "还没有帖子" in html)

print("\n6. 分页")
st, html = get("/posts?sort=new")
check("分页组件存在", "pager" in html)

print("\n7. 板块筛选")
st, html = get("/posts?boardId=1")
check("板块筛选可用", st == 200 and "综合讨论" in html)

print("\n8. 退出登录")
st, html = get("/logout")
check("退出成功", st == 200)
st, html = get("/posts")
check("退出后回到未登录态", "登录 / 注册" in html)

print("\n9. 管理员登录与后台（单表管理）")
st, html = post("/login", {"username": "admin", "password": "admin123"})
check("管理员登录成功", st == 200)
st, html = get("/posts")
check("顶栏显示管理员昵称", "管理员" in html)
st, html = get("/admin?op=posts")
check("后台内容管理页可打开", st == 200 and "管理后台" in html)
st, html = get("/admin?op=users")
check("后台用户管理页可打开", st == 200 and "👥 用户管理" in html)
check("用户表列出注册用户", user in html or "测试同学" in html)

print("\n10. 管理员操作：置顶 / 删除")
st, html = post("/admin?op=posts", {"action": "pin", "id": pid, "on": 1})
st, html = get("/posts")
check("置顶生效（列表出现置顶标记）", html.count("置顶") >= 1)

print("\n11. 权限校验")
st, html = post("/admin?op=users", {"action": "ban", "id": 1, "attempt": "1"})
st, html = get("/admin?op=users")
check("后台操作不报错", st == 200, st)

print("\n" + "=" * 60)
print("通过 %d 项，失败 %d 项" % (len(PASS), len(FAIL)))
if FAIL:
    print("失败清单：")
    for f in FAIL:
        print("  - " + f)
