<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 帖子列表页（首页）· 现代版 =====================
     数据由 PostListServlet 提供：pageBean（分页）、boards（板块）、hotPosts（热门）
     字段沿用真实 Bean：nickname / boardName / replies / likes / views / createdText / summary
--%>

<%-- 首页全屏沉浸 Hero（仅在「全部帖子 + 无搜索」时展示）--%>
<c:if test="${empty boardId || boardId == 0}">
    <c:if test="${empty kw}">
        <section class="hero-fs">
            <div class="hero-fs-inner">
                <span class="hero-chip">欢 迎 来 访</span>
                <h1 class="hero-fs-title">九组论坛</h1>
                <p class="hero-fs-sub">说点什么，写点什么 —— 在时光中相遇，传递温暖话语</p>
                <div class="hero-fs-actions">
                    <a class="hero-btn ghost" href="${ctx}/posts?boardId=1"><svg class="i"><use href="#i-mega"></use></svg>看公告</a>
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

<main class="m-main" id="main-anchor">

    <%-- ========== 左侧：帖子流 ========== --%>
    <section>
        <%-- 操作反馈（Servlet 通过 Session 传入，显示后即清除）--%>
        <c:if test="${not empty sessionScope.flash}">
            <div class="m-alert ok"><c:out value="${sessionScope.flash}"/></div>
            <c:remove var="flash" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="m-alert error"><c:out value="${sessionScope.flashError}"/></div>
            <c:remove var="flashError" scope="session"/>
        </c:if>
        <c:if test="${welcome == '1'}">
            <div class="m-alert ok">注册成功，欢迎来到九组论坛！</div>
        </c:if>

        <%-- AI 助教推广横幅 --%>
        <div class="m-ai-banner">
            <div>
                <strong style="display:block;color:#3730a3;margin-bottom:2px">内置 DeepSeek 大模型问答</strong>
                <span style="font-size:0.83rem;color:#4338ca">遇到技术或学术问题？可以随时向论坛 AI 助教提问。</span>
            </div>
            <a class="m-btn primary sm" href="${ctx}/ai">去咨询</a>
        </div>

        <%-- 板块 + 排序 --%>
        <div class="m-tabs">
            <a class="m-chip ${empty boardId || boardId == 0 ? 'on' : ''}" href="${ctx}/posts">全部板块</a>
            <c:forEach items="${boards}" var="b">
                <a class="m-chip ${boardId == b.id ? 'on' : ''}" href="${ctx}/posts?boardId=${b.id}">
                    <c:out value="${b.name}"/>
                </a>
            </c:forEach>
            <span style="width:8px"></span>
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
            <a class="m-chip ${sort ne 'hot' ? 'on' : ''}" href="${sortNew}">最新</a>
            <a class="m-chip ${sort eq 'hot' ? 'on' : ''}" href="${sortHot}">热门</a>
            <c:if test="${not empty kw}">
                <a class="m-chip" href="${ctx}/posts"><svg class="i"><use href="#i-x"></use></svg>清除「<c:out value="${kw}"/>」</a>
            </c:if>
        </div>

        <%-- 帖子列表 --%>
        <div class="m-card">
            <c:choose>
                <c:when test="${empty pageBean.list}">
                    <div class="m-empty">
                        <c:choose>
                            <c:when test="${not empty kw}">没有找到与「<c:out value="${kw}"/>」相关的帖子</c:when>
                            <c:otherwise>这个板块还没有帖子，来发第一个吧</c:otherwise>
                        </c:choose>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${pageBean.list}" var="p">
                        <article class="m-post">
                            <span class="m-ava lg" style="background:${p.avatarColor}">
                                <c:out value="${p.nickname.substring(0,1)}"/>
                            </span>
                            <div class="m-post-body">
                                <div class="m-post-head">
                                    <c:if test="${p.top}"><span class="m-tag pin">置顶</span></c:if>
                                    <span class="m-tag board"><c:out value="${p.boardName}"/></span>
                                    <a class="m-post-title" href="${ctx}/post/detail?id=${p.id}"><c:out value="${p.title}"/></a>
                                </div>
                                <div class="m-post-desc"><c:out value="${p.summary}"/></div>

                                <c:if test="${not empty p.imagePath}">
                                    <img class="m-post-thumb" src="${ctx}${p.imagePath}" alt="配图" data-zoom>
                                </c:if>

                                <div class="m-meta">
                                    <span class="who"><c:out value="${p.nickname}"/></span>
                                    <span><c:out value="${p.createdText}"/></span>
                                    <span><svg class="i"><use href="#i-eye"></use></svg><c:out value="${p.views}"/></span>
                                    <span><svg class="i"><use href="#i-chat"></use></svg><c:out value="${p.replies}"/></span>
                                    <span><svg class="i"><use href="#i-heart"></use></svg><c:out value="${p.likes}"/></span>
                                    <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq p.userId or sessionScope.user.admin)}">
                                        <span style="margin-left:auto;display:inline-flex;gap:8px;align-items:center">
                                            <a href="${ctx}/post/edit?id=${p.id}" style="color:var(--m-primary);font-size:0.78rem">编辑</a>
                                            <form action="${ctx}/post/delete" method="post" style="display:inline"
                                                  onsubmit="return confirm('确定删除这个帖子？回复也会一起删除')">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <button type="submit" class="m-rm">删除</button>
                                            </form>
                                        </span>
                                    </c:if>
                                </div>
                            </div>
                        </article>
                    </c:forEach>

                    <%-- 分页 --%>
                    <div class="m-pager">
                        <c:if test="${pageBean.hasPrev}">
                            <c:url var="prevUrl" value="/posts">
                                <c:param name="boardId" value="${boardId}"/>
                                <c:param name="kw" value="${kw}"/>
                                <c:param name="sort" value="${sort}"/>
                                <c:param name="pageNo" value="${pageBean.pageNo - 1}"/>
                            </c:url>
                            <a class="m-page" href="${prevUrl}">上一页</a>
                        </c:if>
                        <span class="m-page info">${pageBean.pageNo} / ${empty pageBean.totalPage || pageBean.totalPage == 0 ? 1 : pageBean.totalPage}</span>
                        <c:if test="${pageBean.hasNext}">
                            <c:url var="nextUrl" value="/posts">
                                <c:param name="boardId" value="${boardId}"/>
                                <c:param name="kw" value="${kw}"/>
                                <c:param name="sort" value="${sort}"/>
                                <c:param name="pageNo" value="${pageBean.pageNo + 1}"/>
                            </c:url>
                            <a class="m-page" href="${nextUrl}">下一页</a>
                        </c:if>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <%-- ========== 右侧：创作中心 / 统计 / 热门 / 公告 ========== --%>
    <aside>
        <div class="m-card" style="text-align:center">
            <h4 class="m-widget-title" style="justify-content:center"><svg class="i"><use href="#i-pen"></use></svg>创作中心</h4>
            <p style="font-size:0.83rem;color:var(--m-text-muted);margin-bottom:0.9rem">分享你的见解与技术疑问</p>
            <a class="m-btn primary block" href="${ctx}/post/add">发布新帖子</a>
        </div>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-chart"></use></svg>站点统计</h4>
            <div class="m-stats">
                <div class="m-stat"><b>${totalPosts}</b><span>帖子总数</span></div>
                <div class="m-stat"><b>${totalUsers}</b><span>注册用户</span></div>
            </div>
        </div>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-fire"></use></svg>热门帖子 Top 5</h4>
            <c:choose>
                <c:when test="${empty hotPosts}">
                    <p style="font-size:0.82rem;color:var(--m-text-muted)">还没有热门内容</p>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${hotPosts}" var="h" varStatus="vs">
                        <div class="m-hot">
                            <span class="idx">${vs.index + 1}</span>
                            <a href="${ctx}/post/detail?id=${h.id}" title="${h.title}"><c:out value="${h.title}"/></a>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-note"></use></svg>论坛公告</h4>
            <ul class="m-notice">
                <li>架构：Servlet + JSP + JSTL/EL + JavaBean + JDBC，标准 MVC 分层</li>
                <li>功能：发帖回帖、板块分类、点赞、图片上传、帖子编辑</li>
                <li>智能：内置 DeepSeek AI 助教，支持帮写草稿</li>
                <li>管理：后台可管板块、帖子、回复、用户</li>
            </ul>
        </div>
    </aside>
</main>

<script>
    // 点击图片放大（轻量灯箱）
    document.addEventListener('click', function (e) {
        var img = e.target.closest ? e.target.closest('img[data-zoom]') : null;
        if (!img) { return; }
        var box = document.createElement('div');
        box.className = 'm-lightbox';
        box.innerHTML = '<img src="' + img.src + '" alt="放大">';
        box.onclick = function () { document.body.removeChild(box); };
        document.body.appendChild(box);
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
