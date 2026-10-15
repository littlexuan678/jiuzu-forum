<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 公共头部：引入样式与顶栏，所有页面统一包含此文件（JSP include 复用） --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="九组论坛 - 基于 Java Web 的轻量级论坛系统">
    <link rel="icon" href="${ctx}/static/images/favicon.png">
    <title><c:out value="${empty pageTitle ? '首页' : pageTitle}"/> - 九组论坛</title>
    <script>try{var t=localStorage.getItem('forum-theme');if(t==='dark'){document.documentElement.setAttribute('data-theme','dark');}}catch(e){}</script>
    <link rel="stylesheet" href="${ctx}/static/css/style.v8.css">
    <link rel="stylesheet" href="${ctx}/static/css/style.v9.css">
    <link rel="stylesheet" href="${ctx}/static/css/admin.css">
</head>
<body class="${adminMode ? 'admin-mode' : ''}">

<%-- SVG 图标雪碧图：全站统一线性图标（feather/lucide 风格），通过 <svg class="i"><use href="#i-xxx"/></svg> 引用 --%>
<svg xmlns="http://www.w3.org/2000/svg" style="display:none" aria-hidden="true">
    <symbol id="i-search" viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.5" y2="16.5"/></symbol>
    <symbol id="i-menu" viewBox="0 0 24 24"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></symbol>
    <symbol id="i-pen" viewBox="0 0 24 24"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></symbol>
    <symbol id="i-chat" viewBox="0 0 24 24"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></symbol>
    <symbol id="i-eye" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></symbol>
    <symbol id="i-heart" viewBox="0 0 24 24"><path fill="currentColor" stroke="none" d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></symbol>
    <symbol id="i-heart-o" viewBox="0 0 24 24"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></symbol>
    <symbol id="i-trash" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></symbol>
    <symbol id="i-pin" viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></symbol>
    <symbol id="i-mega" viewBox="0 0 24 24"><path d="m3 11 18-5v12L3 14v-3z"/><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"/></symbol>
    <symbol id="i-folder" viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></symbol>
    <symbol id="i-chart" viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></symbol>
    <symbol id="i-fire" viewBox="0 0 24 24"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/></symbol>
    <symbol id="i-zap" viewBox="0 0 24 24"><path d="M13 2 3 14h9l-1 8 10-12h-9l1-8z"/></symbol>
    <symbol id="i-tool" viewBox="0 0 24 24"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></symbol>
    <symbol id="i-users" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></symbol>
    <symbol id="i-box" viewBox="0 0 24 24"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></symbol>
    <symbol id="i-doc" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="8" y1="13" x2="16" y2="13"/><line x1="8" y1="17" x2="16" y2="17"/></symbol>
    <symbol id="i-grid" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></symbol>
    <symbol id="i-spark" viewBox="0 0 24 24"><path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z"/><path d="M19 14.5l.8 2.3L22 17.6l-2.2.8L19 20.7l-.8-2.3L16 17.6l2.2-.8z"/></symbol>
    <symbol id="i-wave" viewBox="0 0 24 24"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></symbol>
    <symbol id="i-note" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></symbol>
    <symbol id="i-sun" viewBox="0 0 24 24"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></symbol>
    <symbol id="i-moon" viewBox="0 0 24 24"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></symbol>
    <symbol id="i-x" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></symbol>
    <symbol id="i-warn" viewBox="0 0 24 24"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></symbol>
    <symbol id="i-up" viewBox="0 0 24 24"><path d="M12 19V5"/><polyline points="5 12 12 5 19 12"/></symbol>
    <symbol id="i-down" viewBox="0 0 24 24"><path d="M12 5v14"/><polyline points="19 12 12 19 5 12"/></symbol>
</svg>

<header class="m-header" id="top">
    <div class="m-nav">
        <button class="m-icon-btn m-burger" id="burger" type="button" aria-label="菜单"><svg class="i"><use href="#i-menu"></use></svg></button>

        <a class="m-logo" href="${ctx}/posts">
            <img src="${ctx}/static/images/logo.png" alt="九组论坛">
            <span>九组论坛</span>
            <span class="m-badge">社区</span>
        </a>

        <nav class="m-links">
            <a class="m-link ${empty boardId || boardId == 0 ? 'on' : ''}" href="${ctx}/posts">首页</a>
            <a class="m-link" href="${ctx}/ai"><svg class="i"><use href="#i-spark"></use></svg>AI 助手</a>
            <c:if test="${sessionScope.user.admin}">
                <a class="m-link" href="${ctx}/admin?op=posts" style="color:#d97706">管理后台</a>
            </c:if>
        </nav>

        <span class="m-spacer"></span>

        <div class="m-search">
            <form action="${ctx}/posts" method="get">
                <input type="text" name="kw" value="<c:out value='${kw}'/>" placeholder="搜索帖子…">
                <button type="submit" aria-label="搜索"><svg class="i"><use href="#i-search"></use></svg></button>
            </form>
        </div>

        <button class="m-icon-btn" id="themeToggle" type="button" title="切换深色 / 浅色"><svg class="i"><use href="#i-moon"></use></svg></button>

        <div class="m-user">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <a href="${ctx}/user/profile" title="个人中心" style="display:flex;align-items:center;gap:8px">
                        <c:choose>
                            <c:when test="${not empty sessionScope.user.avatarUrl}">
                                <img class="m-ava" src="<c:out value='${sessionScope.user.avatar}'/>" alt="头像" style="object-fit:cover">
                            </c:when>
                            <c:otherwise>
                                <span class="m-ava" style="background:${sessionScope.user.avatarColor}">
                                    <c:out value="${sessionScope.user.avatarText}"/>
                                </span>
                            </c:otherwise>
                        </c:choose>
                        <span><c:out value="${sessionScope.user.nickname}"/></span>
                    </a>
                    <a class="m-btn ghost sm" href="${ctx}/logout">退出</a>
                </c:when>
                <c:otherwise>
                    <a class="m-btn primary sm" href="${ctx}/login">登录 / 注册</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<%-- 右下角 FAB 悬浮按钮列：发帖 / AI 助手 / 回到顶部 --%>
<div class="fab-col">
    <a class="fab" href="${ctx}/post/add" title="发帖"><svg class="i"><use href="#i-pen"></use></svg></a>
    <a class="fab" href="${ctx}/ai" title="AI 助手"><svg class="i"><use href="#i-zap"></use></svg></a>
    <a class="fab" href="#top" title="回到顶部"><svg class="i"><use href="#i-up"></use></svg></a>
</div>

<%-- 移动端抽屉式板块导航 --%>
<c:if test="${not empty boards}">
    <div class="drawer" id="drawer">
        <div class="nav-sec">板块</div>
        <a class="nav-item ${empty boardId || boardId == 0 ? 'on' : ''}" href="${ctx}/posts">
            <span class="ic"><svg class="i"><use href="#i-folder"></use></svg></span><span>全部帖子</span>
        </a>
        <c:forEach items="${boards}" var="b">
            <a class="nav-item ${boardId == b.id ? 'on' : ''}"
               href="${ctx}/posts?boardId=${b.id}">
                <span class="ic"><jsp:include page="/WEB-INF/jsp/common/board-icon.jsp"><jsp:param name="name" value="${b.name}"/><jsp:param name="icon" value="${b.icon}"/></jsp:include></span><span><c:out value="${b.name}"/></span>
                <span class="n"><c:out value="${b.postCount}"/></span>
            </a>
        </c:forEach>
    </div>
    <div class="mask" id="mask" style="display:none"></div>
</c:if>

<script>
    // 移动端抽屉：点汉堡显示，点遮罩、点导航项关闭
    var burger = document.getElementById('burger');
    var drawer = document.getElementById('drawer');
    var mask = document.getElementById('mask');
    if (burger && drawer) {
        burger.onclick = function () { drawer.classList.add('on'); if (mask) mask.style.display = 'block'; };
        if (mask) mask.onclick = function () { drawer.classList.remove('on'); mask.style.display = 'none'; };
        var items = drawer.querySelectorAll('.nav-item');
        for (var i = 0; i < items.length; i++) {
            items[i].onclick = function () { drawer.classList.remove('on'); if (mask) mask.style.display = 'none'; };
        }
    }

    // 主题切换：浅色 / 深色，记忆到 localStorage
    var themeBtn = document.getElementById('themeToggle');
    if (themeBtn) {
        function syncThemeIcon() {
            var dark = document.documentElement.getAttribute('data-theme') === 'dark';
            themeBtn.innerHTML = '<svg class="i"><use href="#' + (dark ? 'i-sun' : 'i-moon') + '"></use></svg>';
        }
        syncThemeIcon();
        themeBtn.onclick = function () {
            var dark = document.documentElement.getAttribute('data-theme') === 'dark';
            if (dark) {
                document.documentElement.removeAttribute('data-theme');
                try { localStorage.setItem('forum-theme', 'light'); } catch (e) {}
            } else {
                document.documentElement.setAttribute('data-theme', 'dark');
                try { localStorage.setItem('forum-theme', 'dark'); } catch (e) {}
            }
            syncThemeIcon();
        };
    }
</script>
