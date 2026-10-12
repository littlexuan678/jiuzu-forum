<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 根据板块名输出对应 SVG 图标（图标定义在 header.jsp 的 SVG 雪碧图中）。
     用法：<jsp:include page="/WEB-INF/jsp/common/board-icon.jsp"><jsp:param name="name" value="${b.name}"/></jsp:include> --%>
<c:choose>
    <c:when test="${param.name eq '技术问答'}"><c:set var="bi" value="i-tool"/></c:when>
    <c:when test="${param.name eq '资源分享'}"><c:set var="bi" value="i-box"/></c:when>
    <c:when test="${param.name eq '闲聊灌水'}"><c:set var="bi" value="i-wave"/></c:when>
    <c:when test="${param.name eq '公告区'}"><c:set var="bi" value="i-mega"/></c:when>
    <c:otherwise><c:set var="bi" value="i-chat"/></c:otherwise>
</c:choose>
<svg class="i" aria-hidden="true"><use href="#${bi}"></use></svg>
