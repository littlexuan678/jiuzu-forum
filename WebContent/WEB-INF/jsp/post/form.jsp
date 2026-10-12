<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 发帖表单 =====================
     GET /post/add 由 PostAddServlet 提供板块列表；POST 提交到同一地址 --%>

<div class="container" style="padding-top:16px;max-width:760px">
    <div class="detail card">
        <h1 style="font-size:19px;margin-bottom:14px">✏️ 发布新帖</h1>

        <c:if test="${not empty error}">
            <div class="alert error"><c:out value="${error}"/></div>
        </c:if>

        <form action="${ctx}/post/add" method="post">
            <div class="field">
                <label for="boardSel">选择板块</label>
                <select class="select" id="boardSel" name="boardId">
                    <c:forEach items="${boards}" var="b">
                        <option value="${b.id}" ${boardId eq b.id ? 'selected' : ''}>
                            <c:out value="${b.icon}"/> <c:out value="${b.name}"/>
                            <c:if test="${not empty b.description}"> — <c:out value="${b.description}"/></c:if>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="title">标题</label>
                <input class="input" id="title" name="title" value=""
                       placeholder="一句话说清你的帖子" maxlength="60" autofocus>
            </div>
            <div class="field">
                <label for="content">正文</label>
                <textarea class="textarea" id="content" name="content" placeholder="写点什么…（支持换行）"></textarea>
            </div>
            <div style="display:flex;gap:8px;align-items:center">
                <button class="btn" type="submit">发布帖子</button>
                <a class="btn ghost" href="${ctx}/posts">取消</a>
                <span class="muted" style="font-size:11.5px;margin-left:auto">标题最多 60 字</span>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
