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
    <link rel="stylesheet" href="${ctx}/static/css/style.css">
</head>
<body>

<header class="header">
    <div class="container inner">
        <button class="burger" id="burger" type="button">☰</button>

        <a class="logo" href="${ctx}/posts">
            <img src="${ctx}/static/images/logo.png" alt="九组论坛">
            <b>九组论坛</b>
        </a>
        <span class="slogan">说点什么，写点什么</span>

        <nav class="nav-links">
            <a href="${ctx}/posts">首页</a>
            <c:if test="${sessionScope.user.admin}">
                <a href="${ctx}/admin?op=posts">管理后台</a>
            </c:if>
        </nav>

        <div class="top-search">
            <form action="${ctx}/posts" method="get">
                <input type="text" name="kw" value="<c:out value='${kw}'/>" placeholder="搜索标题 / 内容…">
                <button type="submit">🔍</button>
            </form>
        </div>

        <div class="top-user">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <span class="ava" style="background:${sessionScope.user.avatarColor}">
                        <c:out value="${sessionScope.user.avatarText}"/>
                    </span>
                    <span class="uname"><c:out value="${sessionScope.user.nickname}"/></span>
                    <a class="btn ghost sm" href="${ctx}/post/add">✏️ 发帖</a>
                    <a class="btn ghost sm" href="${ctx}/logout">退出登录</a>
                </c:when>
                <c:otherwise>
                    <a class="btn" href="${ctx}/login">登录 / 注册</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<%-- 移动端抽屉式板块导航 --%>
<c:if test="${not empty boards}">
    <div class="drawer" id="drawer">
        <div class="nav-sec">板块</div>
        <a class="nav-item ${empty boardId || boardId == 0 ? 'on' : ''}" href="${ctx}/posts">
            <span class="ic">🗂</span><span>全部帖子</span>
        </a>
        <c:forEach items="${boards}" var="b">
            <a class="nav-item ${boardId == b.id ? 'on' : ''}"
               href="${ctx}/posts?boardId=${b.id}">
                <span class="ic"><c:out value="${b.icon}"/></span><span><c:out value="${b.name}"/></span>
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
</script>
