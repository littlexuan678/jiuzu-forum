<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
<div class="container" style="padding:110px 12px;text-align:center">
    <div class="card" style="padding:50px 20px">
        <div class="err-icon"><svg class="i"><use href="#i-search"></use></svg></div>
        <h2 style="font-size:18px;margin-bottom:8px">页面走丢了</h2>
        <p class="muted" style="font-size:13px">
            <c:out value="${empty message ? '你访问的资源不存在或已被删除' : message}"/>
        </p>
        <a class="btn" style="margin-top:16px" href="${ctx}/posts">回到首页</a>
    </div>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
