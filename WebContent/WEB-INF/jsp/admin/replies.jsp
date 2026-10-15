<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 后台 · 回复管理 =====================
     数据由 AdminServlet 提供：pageBean（回复分页，含 postTitle / boardName） --%>

<div class="container admin-wrap">
    <div class="admin-head">
        <h2><svg class="i"><use href="#i-chat"></use></svg>回复管理</h2>
        <span class="desc">查看并删除不当回复</span>
        <div class="admin-nav">
            <a class="sort" href="${ctx}/admin?op=dashboard">数据统计</a>
            <a class="sort" href="${ctx}/admin?op=posts">内容管理</a>
            <a class="sort" href="${ctx}/admin?op=boards">板块管理</a>
            <a class="sort on" href="${ctx}/admin?op=replies">回复管理</a>
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

    <table class="tbl">
        <thead>
        <tr>
            <th style="width:52px">ID</th>
            <th>回复内容</th>
            <th style="width:150px">所属帖子</th>
            <th style="width:90px">板块</th>
            <th style="width:120px">作者</th>
            <th style="width:140px">来源 IP</th>
            <th style="width:130px">时间</th>
            <th style="width:80px">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty pageBean.list}">
                <tr><td colspan="8" style="text-align:center;padding:40px" class="muted">暂无回复</td></tr>
            </c:when>
            <c:otherwise>
                <c:forEach items="${pageBean.list}" var="r">
                    <tr>
                        <td><c:out value="${r.id}"/></td>
                        <td class="reply-cell"><c:out value="${r.content}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty r.postTitle}">
                                    <a href="${ctx}/post/detail?id=${r.postId}"><c:out value="${r.postTitle}"/></a>
                                </c:when>
                                <c:otherwise><span class="muted">（帖子已删）</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${r.boardName}"/></td>
                        <td><c:out value="${r.nickname}"/></td>
                        <td class="muted" style="font-size:12px">
                            <c:choose>
                                <c:when test="${not empty r.ip}">
                                    <c:out value="${r.ip}"/>
                                    <c:if test="${not empty r.ipLoc}">
                                        <div style="font-size:11px;opacity:.75"><c:out value="${r.ipLoc}"/></div>
                                    </c:if>
                                </c:when>
                                <c:otherwise>—</c:otherwise>
                            </c:choose>
                        </td>
                        <td class="muted" style="font-size:12px">
                            <fmt:formatDate value="${r.createdDate}" pattern="yyyy-MM-dd HH:mm"/>
                        </td>
                        <td>
                            <form action="${ctx}/admin?op=replies" method="post"
                                  onsubmit="return confirm('确定删除这条回复？')">
                                <input type="hidden" name="action" value="delReply">
                                <input type="hidden" name="id" value="${r.id}">
                                <button class="btn danger sm" type="submit">删除</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>

    <div class="pager">
        <c:if test="${pageBean.hasPrev}">
            <a href="${ctx}/admin?op=replies&pageNo=${pageBean.pageNo - 1}">上一页</a>
        </c:if>
        <span class="cur">${pageBean.pageNo} / ${empty pageBean.totalPage || pageBean.totalPage == 0 ? 1 : pageBean.totalPage}</span>
        <c:if test="${pageBean.hasNext}">
            <a href="${ctx}/admin?op=replies&pageNo=${pageBean.pageNo + 1}">下一页</a>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
