<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 入口页：直接进入帖子列表（遵循 MVC，所有请求都先经过 Servlet 处理） --%>
<%
    response.sendRedirect(request.getContextPath() + "/posts");
%>
