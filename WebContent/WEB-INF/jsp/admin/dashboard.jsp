<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 后台 · 数据统计 =====================
     数据由 AdminServlet 提供：statUsers / statPosts / statReplies /
     statBoards / statNewPostsToday --%>

<div class="container admin-wrap">
    <div class="admin-head">
        <h2><svg class="i"><use href="#i-chart"></use></svg>数据统计</h2>
        <span class="desc">论坛整体运营概况</span>
        <div class="admin-nav">
            <a class="sort on" href="${ctx}/admin?op=dashboard">数据统计</a>
            <a class="sort" href="${ctx}/admin?op=posts">内容管理</a>
            <a class="sort" href="${ctx}/admin?op=boards">板块管理</a>
            <a class="sort" href="${ctx}/admin?op=replies">回复管理</a>
            <a class="sort" href="${ctx}/admin?op=users">用户管理</a>
            <a class="sort" href="${ctx}/posts">返回前台</a>
        </div>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="alert ok"><c:out value="${sessionScope.flash}"/></div>
        <c:remove var="flash" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert error"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <div class="stat-grid">
        <div class="stat-card">
            <span class="stat-ic" style="background:rgba(0,180,216,.14);color:#0094b5"><svg class="i"><use href="#i-users"></use></svg></span>
            <div class="stat-num"><c:out value="${statUsers}"/></div>
            <div class="stat-label">注册用户</div>
        </div>
        <div class="stat-card">
            <span class="stat-ic" style="background:rgba(255,138,0,.14);color:#e08a00"><svg class="i"><use href="#i-doc"></use></svg></span>
            <div class="stat-num"><c:out value="${statPosts}"/></div>
            <div class="stat-label">主题帖子</div>
        </div>
        <div class="stat-card">
            <span class="stat-ic" style="background:rgba(124,92,255,.14);color:#7c5cff"><svg class="i"><use href="#i-chat"></use></svg></span>
            <div class="stat-num"><c:out value="${statReplies}"/></div>
            <div class="stat-label">回复总数</div>
        </div>
        <div class="stat-card">
            <span class="stat-ic" style="background:rgba(46,204,113,.14);color:#27ae60"><svg class="i"><use href="#i-grid"></use></svg></span>
            <div class="stat-num"><c:out value="${statBoards}"/></div>
            <div class="stat-label">板块数量</div>
        </div>
        <div class="stat-card">
            <span class="stat-ic" style="background:rgba(231,76,60,.14);color:#e74c3c"><svg class="i"><use href="#i-fire"></use></svg></span>
            <div class="stat-num"><c:out value="${statNewPostsToday}"/></div>
            <div class="stat-label">今日新帖</div>
        </div>
    </div>

    <div class="card quick-panel">
        <h3 class="card-title">快捷操作</h3>
        <div class="quick-row">
            <a class="btn ghost" href="${ctx}/admin?op=posts"><svg class="i"><use href="#i-doc"></use></svg>管理帖子</a>
            <a class="btn ghost" href="${ctx}/admin?op=boards"><svg class="i"><use href="#i-grid"></use></svg>管理板块</a>
            <a class="btn ghost" href="${ctx}/admin?op=replies"><svg class="i"><use href="#i-chat"></use></svg>审查回复</a>
            <a class="btn ghost" href="${ctx}/admin?op=users"><svg class="i"><use href="#i-users"></use></svg>管理用户</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
