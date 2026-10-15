<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 板块图标：优先使用数据库里存的 emoji 图标；为空时按板块名匹配线性 SVG 图标。
     用法：<jsp:include page="/WEB-INF/jsp/common/board-icon.jsp">
             <jsp:param name="name" value="${b.name}"/>
             <jsp:param name="icon" value="${b.icon}"/>
           </jsp:include> --%>
<c:choose>
    <c:when test="${not empty param.icon}">
        <span class="b-emoji"><c:out value="${param.icon}"/></span>
    </c:when>
    <c:when test="${param.name eq '技术问答'}"><svg class="i"><use href="#i-tool"></use></svg></c:when>
    <c:when test="${param.name eq '资源分享'}"><svg class="i"><use href="#i-box"></use></svg></c:when>
    <c:when test="${param.name eq '综合讨论'}"><svg class="i"><use href="#i-chat"></use></svg></c:when>
    <c:otherwise><svg class="i"><use href="#i-chat"></use></svg></c:otherwise>
</c:choose>
