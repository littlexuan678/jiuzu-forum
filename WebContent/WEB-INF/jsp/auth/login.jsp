<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- 登录 / 注册页：两个 tab 共用一个入口，由 RegisterServlet / LoginServlet 分别处理 --%>
<div class="gate">
    <div class="gate-box">
        <img class="gate-logo" src="${ctx}/static/images/logo.png" alt="九组论坛">
        <img class="gate-text" src="${ctx}/static/images/logo-text.png" alt="第九小组">
        <h1>九组论坛</h1>
        <p class="gate-sub">说点什么，写点什么</p>

        <div class="tabs">
            <button type="button" id="tabLogin" class="on">登录</button>
            <button type="button" id="tabReg">注册</button>
        </div>

        <%-- 操作提示：由 Servlet 的 request 属性传入 --%>
        <c:if test="${not empty error}">
            <div class="alert error"><c:out value="${error}"/></div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="alert ok"><c:out value="${message}"/></div>
        </c:if>
        <c:if test="${not empty banned}">
            <div class="alert error">该账号已被封禁，请联系管理员</div>
        </c:if>

        <%-- 登录表单 --%>
        <form class="gate-form" id="formLogin" action="${ctx}/login" method="post">
            <c:if test="${not empty redirect}">
                <input type="hidden" name="redirect" value="<c:out value='${redirect}'/>">
            </c:if>
            <div class="field">
                <label for="lgUser">用户名</label>
                <input class="input" id="lgUser" name="username" value="" placeholder="请输入用户名" autofocus>
            </div>
            <div class="field">
                <label for="lgPwd">密码</label>
                <input class="input" id="lgPwd" name="password" type="password" placeholder="请输入密码">
            </div>
            <button class="btn" type="submit">登录</button>
            <p class="gate-tip">演示账号：admin / admin123（管理员）</p>
        </form>

        <%-- 注册表单（默认隐藏） --%>
        <form class="gate-form hidden" id="formReg" action="${ctx}/register" method="post">
            <div class="field">
                <label for="rgUser">用户名</label>
                <input class="input" id="rgUser" name="username" value="<c:out value='${username}'/>"
                       placeholder="4~16 位字母、数字或下划线">
            </div>
            <div class="field">
                <label for="rgNick">昵称</label>
                <input class="input" id="rgNick" name="nickname" value="<c:out value='${nickname}'/>"
                       placeholder="显示名称，可留空">
            </div>
            <div class="field">
                <label for="rgPwd">密码</label>
                <input class="input" id="rgPwd" name="password" type="password" placeholder="6~20 位">
            </div>
            <div class="field">
                <label for="rgPwd2">确认密码</label>
                <input class="input" id="rgPwd2" name="confirm" type="password" placeholder="再输入一次">
            </div>
            <button class="btn" type="submit">注册并进入</button>
            <p class="gate-tip">注册成功后会自动登录</p>
        </form>
    </div>
</div>

<script>
    (function () {
        var tabLogin = document.getElementById('tabLogin');
        var tabReg = document.getElementById('tabReg');
        var formLogin = document.getElementById('formLogin');
        var formReg = document.getElementById('formReg');

        function switchTo(isReg) {
            tabLogin.className = isReg ? '' : 'on';
            tabReg.className = isReg ? 'on' : '';
            formLogin.className = isReg ? 'gate-form hidden' : 'gate-form';
            formReg.className = isReg ? 'gate-form' : 'gate-form hidden';
        }

        tabLogin.onclick = function () { switchTo(false); };
        tabReg.onclick = function () { switchTo(true); };

        // 注册失败或访问 /register 时默认展示注册表单
        if (window.location.pathname.indexOf('/register') > -1) {
            switchTo(true);
        }
    })();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
