<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 帖子列表页（首页）=====================
     数据由 PostListServlet 提供：pageBean（分页数据）、boards（板块）、hotPosts（热门） --%>

<%-- 首页全屏沉浸 Hero（朱朱Blog 风格）：仅在「全部帖子 + 无搜索」时展示 --%>
<c:if test="${empty boardId || boardId == 0}">
    <c:if test="${empty kw}">
        <section class="hero-fs">
            <div class="hero-fs-inner">
                <span class="hero-chip">欢 迎 来 访</span>
                <h1 class="hero-fs-title">九组论坛</h1>
                <p class="hero-fs-sub">说点什么，写点什么 —— 在时光中相遇，传递温暖话语</p>
                <div class="hero-fs-actions">
                    <a class="hero-btn ghost" href="${ctx}/posts?boardId=5"><svg class="i"><use href="#i-mega"></use></svg>看公告</a>
                    <c:choose>
                        <c:when test="${not empty sessionScope.user}">
                            <a class="hero-btn primary" href="${ctx}/post/add"><svg class="i"><use href="#i-pen"></use></svg>发个帖子</a>
                        </c:when>
                        <c:otherwise>
                            <a class="hero-btn primary" href="${ctx}/login">登录 / 注册</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <a class="hero-down" href="#main-anchor" aria-label="往下浏览"><svg class="i"><use href="#i-down"></use></svg></a>
        </section>
        <script>if (document.querySelector('.hero-fs')) document.body.classList.add('has-hero');</script>
    </c:if>
</c:if>

<div class="container main" id="main-anchor">

    <%-- ---------- 左侧：板块导航 ---------- --%>
    <aside class="col-nav">
        <div class="card panel" style="padding:12px 10px">
            <div class="nav-sec">板块</div>
            <a class="nav-item ${empty boardId || boardId == 0 ? 'on' : ''}" href="${ctx}/posts">
                <span class="ic"><svg class="i"><use href="#i-folder"></use></svg></span><span>全部帖子</span>
            </a>
            <c:forEach items="${boards}" var="b">
                <a class="nav-item ${boardId == b.id ? 'on' : ''}" href="${ctx}/posts?boardId=${b.id}">
                    <span class="ic"><jsp:include page="/WEB-INF/jsp/common/board-icon.jsp"><jsp:param name="name" value="${b.name}"/></jsp:include></span>
                    <span><c:out value="${b.name}"/></span>
                    <span class="n"><c:out value="${b.postCount}"/></span>
                </a>
            </c:forEach>
        </div>
    </aside>

    <%-- ---------- 中间：帖子流 ---------- --%>
    <section>
        <%-- 操作反馈：由后台 Servlet 通过 Session 传入，显示后即清除 --%>
        <c:if test="${not empty sessionScope.flash}">
            <div class="alert ok"><c:out value="${sessionScope.flash}"/></div>
            <c:remove var="flash" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert error"><c:out value="${sessionScope.flashError}"/></div>
            <c:remove var="flashError" scope="session"/>
        </c:if>
        <c:if test="${welcome == '1'}">
            <div class="alert ok">注册成功，欢迎来到九组论坛！</div>
        </c:if>

        <div class="list-head">
            <h3><c:out value="${pageTitle}"/></h3>
            <c:if test="${not empty board}">
                <span class="desc"><c:out value="${board.description}"/></span>
            </c:if>
            <div class="sorts">
                <c:url var="sortNew" value="/posts">
                    <c:param name="boardId" value="${boardId}"/>
                    <c:param name="kw" value="${kw}"/>
                    <c:param name="sort" value="new"/>
                </c:url>
                <c:url var="sortHot" value="/posts">
                    <c:param name="boardId" value="${boardId}"/>
                    <c:param name="kw" value="${kw}"/>
                    <c:param name="sort" value="hot"/>
                </c:url>
                <a class="sort ${sort ne 'hot' ? 'on' : ''}" href="${sortNew}">最新</a>
                <a class="sort ${sort eq 'hot' ? 'on' : ''}" href="${sortHot}">热门</a>
                <c:if test="${not empty kw}">
                    <a class="sort" href="${ctx}/posts"><svg class="i"><use href="#i-x"></use></svg>清除搜索</a>
                </c:if>
            </div>
        </div>

        <%-- 帖子列表：JSTL 遍历分页数据 --%>
        <c:choose>
            <c:when test="${empty pageBean.list}">
                <div class="card empty">
                    <span class="big"><svg class="i"><use href="#i-note"></use></svg></span>
                    <c:choose>
                        <c:when test="${not empty kw}">没有找到与「<c:out value="${kw}"/>」相关的帖子</c:when>
                        <c:otherwise>这个板块还没有帖子，来发第一个吧</c:otherwise>
                    </c:choose>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${pageBean.list}" var="p">
                    <div class="post-card card">
                        <%-- 自己发的帖或管理员可以删除 --%>
                        <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq p.userId or sessionScope.user.admin)}">
                            <form action="${ctx}/post/delete" method="post"
                                  onsubmit="return confirm('确定删除这个帖子？回复也会一起删除')">
                                <input type="hidden" name="id" value="${p.id}">
                                <button class="pc-del" type="submit"><svg class="i"><use href="#i-trash"></use></svg>删除</button>
                            </form>
                        </c:if>
                        <div class="pc-body">
                            <div class="pc-title">
                                <c:if test="${p.top}"><span class="tag-pin">置顶</span> </c:if>
                                <a href="${ctx}/post/detail?id=${p.id}"><c:out value="${p.title}"/></a>
                            </div>
                            <p class="pc-ex"><c:out value="${p.summary}"/></p>
                            <div class="pc-meta">
                                <span class="who">
                                    <span class="ava xs" style="background:${p.avatarColor}">
                                        <c:out value="${p.nickname.substring(0,1)}"/>
                                    </span>
                                    <c:out value="${p.nickname}"/>
                                </span>
                                <span class="tag-board ${p.boardName eq '公告区' ? 'admin' : ''}">
                                    <jsp:include page="/WEB-INF/jsp/common/board-icon.jsp"><jsp:param name="name" value="${p.boardName}"/></jsp:include>
                                    <c:out value="${p.boardName}"/>
                                </span>
                                <span><c:out value="${p.createdText}"/></span>
                                <span class="nums">
                                    <span><svg class="i"><use href="#i-eye"></use></svg><c:out value="${p.views}"/></span>
                                    <span><svg class="i"><use href="#i-chat"></use></svg><c:out value="${p.replies}"/></span>
                                    <span><svg class="i"><use href="#i-heart"></use></svg><c:out value="${p.likes}"/></span>
                                </span>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <%-- 分页导航 --%>
                <div class="pager">
                    <c:if test="${pageBean.hasPrev}">
                        <c:url var="prevUrl" value="/posts">
                            <c:param name="boardId" value="${boardId}"/>
                            <c:param name="kw" value="${kw}"/>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="pageNo" value="${pageBean.pageNo - 1}"/>
                        </c:url>
                        <a href="${prevUrl}">上一页</a>
                    </c:if>
                    <span class="cur">${pageBean.pageNo} / ${empty pageBean.totalPage || pageBean.totalPage == 0 ? 1 : pageBean.totalPage}</span>
                    <c:if test="${pageBean.hasNext}">
                        <c:url var="nextUrl" value="/posts">
                            <c:param name="boardId" value="${boardId}"/>
                            <c:param name="kw" value="${kw}"/>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="pageNo" value="${pageBean.pageNo + 1}"/>
                        </c:url>
                        <a href="${nextUrl}">下一页</a>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- ---------- 右侧：统计与热门 ---------- --%>
    <aside class="col-rail">
        <div class="card panel">
            <h4><svg class="i"><use href="#i-chart"></use></svg>站点统计</h4>
            <div class="stats">
                <div class="stat"><b>${totalPosts}</b><span>帖子总数</span></div>
                <div class="stat"><b>${totalUsers}</b><span>注册用户</span></div>
            </div>
        </div>

        <div class="card panel">
            <h4><svg class="i"><use href="#i-fire"></use></svg>热门帖子 <small>Top 5</small></h4>
            <c:choose>
                <c:when test="${empty hotPosts}">
                    <p class="muted" style="font-size:12px">还没有热门内容</p>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${hotPosts}" var="h" varStatus="vs">
                        <div class="hot-item">
                            <span class="idx">${vs.index + 1}</span>
                            <a href="${ctx}/post/detail?id=${h.id}" title="${h.title}">
                                <c:out value="${h.title}"/>
                            </a>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="card panel">
            <h4><svg class="i"><use href="#i-zap"></use></svg>快速开始</h4>
            <p style="font-size:12.5px;line-height:1.9;color:var(--color-text-secondary)">
                还没账号？<a href="${ctx}/register">注册一个</a><br>
                有想法？<a href="${ctx}/post/add">发个帖子</a><br>
                有问题？<a href="${ctx}/ai">问问 AI 小九</a><br>
                <a href="${ctx}/posts?boardId=5">查看公告</a>
            </p>
        </div>
    </aside>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
