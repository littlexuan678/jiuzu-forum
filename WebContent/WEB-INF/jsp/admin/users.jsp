<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 后台 · 用户管理 =====================
     数据由 AdminServlet 提供：pageBean（用户分页数据），体现单表数据管理（增删改查） --%>

<div class="container admin-wrap">
    <div class="admin-head">
        <h2><svg class="i"><use href="#i-users"></use></svg>用户管理</h2>
        <span class="desc">封禁 / 解封、权限调整、删除用户</span>
        <div class="admin-nav">
            <a class="sort" href="${ctx}/admin?op=dashboard">数据统计</a>
            <a class="sort" href="${ctx}/admin?op=posts">内容管理</a>
            <a class="sort" href="${ctx}/admin?op=boards">板块管理</a>
            <a class="sort" href="${ctx}/admin?op=replies">回复管理</a>
            <a class="sort on" href="${ctx}/admin?op=users">用户管理</a>
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
            <th style="width:150px">用户名</th>
            <th style="width:120px">昵称</th>
            <th style="width:150px">注册时间</th>
            <th style="width:120px">状态</th>
            <th style="width:220px">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${pageBean.list}" var="u">
            <tr>
                <td><c:out value="${u.id}"/></td>
                <td>
                    <span class="ava xs" style="background:${u.avatarColor}"><c:out value="${u.avatarText}"/></span>
                    <c:out value="${u.username}"/>
                    <c:if test="${u.admin}"><span class="tag info">管理员</span></c:if>
                </td>
                <td><c:out value="${u.nickname}"/></td>
                <td class="muted" style="font-size:12px">
                    <fmt:formatDate value="${u.createdDate}" pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${u.banned}"><span class="tag warn">已封禁</span></c:when>
                        <c:otherwise><span class="tag ok">正常</span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <div class="tbl-actions">
                        <c:choose>
                            <c:when test="${u.username eq 'admin'}">
                                <span class="muted" style="font-size:11.5px">内置账号</span>
                            </c:when>
                            <c:otherwise>
                                <form action="${ctx}/admin?op=users" method="post">
                                    <input type="hidden" name="action" value="${u.banned ? 'unban' : 'ban'}">
                                    <input type="hidden" name="id" value="${u.id}">
                                    <button class="btn ghost sm" type="submit">${u.banned ? '解除封禁' : '封禁'}</button>
                                </form>
                                <form action="${ctx}/admin?op=users" method="post">
                                    <input type="hidden" name="action" value="role">
                                    <input type="hidden" name="id" value="${u.id}">
                                    <input type="hidden" name="role" value="${u.admin ? 0 : 1}">
                                    <button class="btn ghost sm" type="submit">${u.admin ? '取消管理员' : '设为管理员'}</button>
                                </form>
                                <form action="${ctx}/admin?op=users" method="post"
                                      onsubmit="return confirm('确定删除该用户？其回复会一并删除')">
                                    <input type="hidden" name="action" value="delUser">
                                    <input type="hidden" name="id" value="${u.id}">
                                    <button class="btn danger sm" type="submit">删除</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <div class="pager">
        <c:if test="${pageBean.hasPrev}">
            <a href="${ctx}/admin?op=users&pageNo=${pageBean.pageNo - 1}">上一页</a>
        </c:if>
        <span class="cur">${pageBean.pageNo} / ${empty pageBean.totalPage || pageBean.totalPage == 0 ? 1 : pageBean.totalPage}</span>
        <c:if test="${pageBean.hasNext}">
            <a href="${ctx}/admin?op=users&pageNo=${pageBean.pageNo + 1}">下一页</a>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
