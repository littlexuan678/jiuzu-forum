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
        <h3 class="card-title">系统管理 · 点击卡片进入对应模块</h3>
        <div class="console-grid">
            <a class="console-card" href="${ctx}/admin?op=posts">
                <span class="console-ic" style="background:rgba(255,138,0,.16);color:#fbbf24">
                    <svg class="i"><use href="#i-doc"></use></svg>
                </span>
                <h4>帖子管理</h4>
                <p>查看全部主题帖，支持置顶 / 取消置顶、删除违规内容</p>
                <span class="go">进入管理 →</span>
            </a>
            <a class="console-card" href="${ctx}/admin?op=replies">
                <span class="console-ic" style="background:rgba(124,92,255,.16);color:#a78bfa">
                    <svg class="i"><use href="#i-chat"></use></svg>
                </span>
                <h4>回帖管理</h4>
                <p>审查全部楼层回复，一键清理灌水与不当言论</p>
                <span class="go">进入管理 →</span>
            </a>
            <a class="console-card" href="${ctx}/admin?op=boards">
                <span class="console-ic" style="background:rgba(46,204,113,.16);color:#4ade80">
                    <svg class="i"><use href="#i-grid"></use></svg>
                </span>
                <h4>板块管理</h4>
                <p>新增、改名、调整排序与删除讨论分区</p>
                <span class="go">进入管理 →</span>
            </a>
            <a class="console-card" href="${ctx}/admin?op=users">
                <span class="console-ic" style="background:rgba(0,180,216,.16);color:#38bdf8">
                    <svg class="i"><use href="#i-users"></use></svg>
                </span>
                <h4>用户管理</h4>
                <p>查看注册用户、封禁 / 解封、提升为管理员</p>
                <span class="go">进入管理 →</span>
            </a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
