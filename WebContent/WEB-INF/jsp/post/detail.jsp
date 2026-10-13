<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 帖子详情页 =====================
     数据由 PostDetailServlet 提供：post（帖子）、replies（回复列表） --%>

<div class="container" style="padding-top:92px">
    <%-- Session 中的操作反馈 --%>
    <c:if test="${not empty sessionScope.flash}">
        <div class="alert ok"><c:out value="${sessionScope.flash}"/></div>
        <c:remove var="flash" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert error"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <%-- 帖子正文 --%>
    <article class="detail card">
        <h1>
            <c:if test="${post.top}"><span class="tag-pin">置顶</span> </c:if>
            <c:out value="${post.title}"/>
        </h1>
        <div class="dm">
            <span class="who">
                <span class="ava" style="background:${post.avatarColor}">
                    <c:out value="${post.nickname.substring(0,1)}"/>
                </span>
                <c:out value="${post.nickname}"/>
            </span>
            <span><c:out value="${post.createdText}"/></span>
            <span><svg class="i"><use href="#i-eye"></use></svg><c:out value="${post.views}"/> 次浏览</span>
            <span class="tag-board ${post.boardName eq '公告区' ? 'admin' : ''}">
                <jsp:include page="/WEB-INF/jsp/common/board-icon.jsp"><jsp:param name="name" value="${post.boardName}"/></jsp:include>
                <c:out value="${post.boardName}"/>
            </span>
        </div>

        <div class="detail-body"><c:out value="${post.content}"/></div>

        <div class="detail-acts">
            <%-- 点赞：已点赞显示“已赞” --%>
            <c:choose>
                <c:when test="${empty sessionScope.user}">
                    <a class="btn ghost" href="${ctx}/login?redirect=${pageContext.request.requestURI}%3Fid%3D${post.id}">
                        <svg class="i"><use href="#i-heart-o"></use></svg>赞 <c:out value="${post.likes}"/>
                    </a>
                </c:when>
                <c:otherwise>
                    <a class="btn ${post.liked ? '' : 'ghost'}" href="${ctx}/like?id=${post.id}">
                        <svg class="i"><use href="${post.liked ? '#i-heart' : '#i-heart-o'}"></use></svg>${post.liked ? '已赞' : '赞'} <c:out value="${post.likes}"/>
                    </a>
                </c:otherwise>
            </c:choose>

            <a class="btn ghost" href="${ctx}/posts?boardId=${post.boardId}">返回板块</a>
            <a class="btn ghost" href="${ctx}/posts">全部帖子</a>

            <%-- 作者本人或管理员可删除 --%>
            <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq post.userId or sessionScope.user.admin)}">
                <form action="${ctx}/post/delete" method="post" style="margin-left:auto"
                      onsubmit="return confirm('确定删除这个帖子？回复也会一起删除')">
                    <input type="hidden" name="id" value="${post.id}">
                    <button class="btn danger" type="submit">删除帖子</button>
                </form>
            </c:if>
        </div>
    </article>

    <%-- 回复区 --%>
    <div class="reply-sec">
        <h4>全部回复（<c:out value="${empty replies ? 0 : replies.size()}"/>）</h4>
        <c:choose>
            <c:when test="${empty replies}">
                <div class="card empty" style="padding:30px"><svg class="i"><use href="#i-chat"></use></svg>还没有人回复，来抢沙发</div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${replies}" var="r">
                    <div class="reply card">
                        <div class="rm">
                            <span class="who">
                                <span class="ava xs" style="background:${r.avatarColor}">
                                    <c:out value="${r.nickname.substring(0,1)}"/>
                                </span>
                                <c:out value="${r.nickname}"/>
                            </span>
                            <span><c:out value="${r.createdText}"/></span>
                            <%-- 回复者本人或管理员可删除 --%>
                            <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq r.userId or sessionScope.user.admin)}">
                                <form action="${ctx}/reply/delete" method="post" style="margin-left:auto"
                                      onsubmit="return confirm('确定删除这条回复？')">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <button class="rdel" type="submit">删除</button>
                                </form>
                            </c:if>
                        </div>
                        <div class="rc"><c:out value="${r.content}"/></div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 回复输入框 --%>
    <div class="reply-box card" style="margin-top:10px">
        <c:choose>
            <c:when test="${empty sessionScope.user}">
                <div class="empty" style="padding:20px">
                    <a class="btn" href="${ctx}/login">登录后参与回复</a>
                </div>
            </c:when>
            <c:otherwise>
                <form action="${ctx}/reply/add" method="post">
                    <input type="hidden" name="postId" value="${post.id}">
                    <textarea class="textarea" name="content" placeholder="写下你的看法…"></textarea>
                    <div class="rb-foot">
                        <span class="tip">Ctrl / ⌘ + Enter 发送</span>
                        <button class="btn" type="submit">发表回复</button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
    // 回复框支持 Ctrl/⌘ + Enter 提交
    var ta = document.querySelector('.reply-box textarea');
    if (ta) {
        ta.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                e.preventDefault();
                ta.form.submit();
            }
        });
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
