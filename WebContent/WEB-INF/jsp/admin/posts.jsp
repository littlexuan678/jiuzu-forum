<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 后台 · 内容管理 =====================
     数据由 AdminServlet 提供：pageBean（帖子分页数据） --%>

<div class="container admin-wrap">
    <div class="admin-head">
        <h2><svg class="i"><use href="#i-tool"></use></svg>管理后台</h2>
        <span class="desc">帖子管理与用户管理</span>
        <div class="admin-nav">
            <a class="sort" href="${ctx}/admin?op=dashboard">数据统计</a>
            <a class="sort on" href="${ctx}/admin?op=posts">内容管理</a>
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

    <table class="tbl">
        <thead>
        <tr>
            <th style="width:52px">ID</th>
            <th>标题</th>
            <th style="width:110px">板块</th>
            <th style="width:100px">作者</th>
            <th style="width:130px">数据</th>
            <th style="width:170px">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty pageBean.list}">
                <tr><td colspan="6" style="text-align:center;padding:40px" class="muted">暂无帖子</td></tr>
            </c:when>
            <c:otherwise>
                <c:forEach items="${pageBean.list}" var="p">
                    <tr>
                        <td><c:out value="${p.id}"/></td>
                        <td>
                            <c:if test="${p.top}"><span class="tag-pin">置顶</span> </c:if>
                            <a href="${ctx}/post/detail?id=${p.id}"><c:out value="${p.title}"/></a>
                        </td>
                        <td><span class="tag-board"><c:out value="${p.boardName}"/></span></td>
                        <td><c:out value="${p.nickname}"/></td>
                        <td class="muted" style="font-size:12px">
                            <span class="tbl-stat"><svg class="i"><use href="#i-eye"></use></svg><c:out value="${p.views}"/></span>
                            <span class="tbl-stat"><svg class="i"><use href="#i-chat"></use></svg><c:out value="${p.replies}"/></span>
                            <span class="tbl-stat"><svg class="i"><use href="#i-heart"></use></svg><c:out value="${p.likes}"/></span>
                        </td>
                        <td>
                            <div class="tbl-actions">
                                <form action="${ctx}/admin?op=posts" method="post">
                                    <input type="hidden" name="action" value="pin">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <input type="hidden" name="on" value="${p.top ? 0 : 1}">
                                    <button class="btn ghost sm" type="submit">${p.top ? '取消置顶' : '置顶'}</button>
                                </form>
                                <form action="${ctx}/admin?op=posts" method="post"
                                      onsubmit="return confirm('确定删除该帖子？回复会一起删除')">
                                    <input type="hidden" name="action" value="delPost">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <button class="btn danger sm" type="submit">删除</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>

    <%-- 分页 --%>
    <div class="pager">
        <c:if test="${pageBean.hasPrev}">
            <a href="${ctx}/admin?op=posts&pageNo=${pageBean.pageNo - 1}">上一页</a>
        </c:if>
        <span class="cur">${pageBean.pageNo} / ${empty pageBean.totalPage || pageBean.totalPage == 0 ? 1 : pageBean.totalPage}</span>
        <c:if test="${pageBean.hasNext}">
            <a href="${ctx}/admin?op=posts&pageNo=${pageBean.pageNo + 1}">下一页</a>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
