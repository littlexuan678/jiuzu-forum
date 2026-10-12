<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 公共页脚 --%>
<footer class="site-footer">
    <div class="container">
        <div>九组论坛 · Java Web 课程设计作品</div>
        <div class="f-sub">Servlet + JSP + JSTL/EL + JavaBean + JDBC · MVC 分层架构</div>
        <div style="margin-top:8px">
            <a href="${ctx}/posts">首页</a> ·
            <a href="${ctx}/login">登录</a>
            <c:if test="${sessionScope.user.admin}"> · <a href="${ctx}/admin?op=posts">管理后台</a></c:if>
        </div>
    </div>
</footer>
</body>
</html>
