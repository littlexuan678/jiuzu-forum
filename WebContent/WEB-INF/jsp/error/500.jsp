<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
<div class="container" style="padding:80px 12px;text-align:center">
    <div class="card" style="padding:50px 20px">
        <div style="font-size:52px;margin-bottom:12px">⚠️</div>
        <h2 style="font-size:18px;margin-bottom:8px">服务器开小差了</h2>
        <p class="muted" style="font-size:13px">请稍后重试，或联系管理员。</p>
        <a class="btn" style="margin-top:16px" href="${ctx}/posts">回到首页</a>
    </div>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
