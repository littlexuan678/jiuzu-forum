<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 个人中心 =====================
     数据：ProfileServlet 提供 user（当前登录用户的最新信息）
     功能：上传更换头像（走 /upload）、修改昵称、修改密码
--%>

<main class="m-main profile-main">
    <section>
        <c:if test="${not empty message}">
            <div class="m-alert ok"><c:out value="${message}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="m-alert error"><c:out value="${error}"/></div>
        </c:if>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-users"></use></svg>个人基本信息</h4>

            <form action="${ctx}/user/profile" method="post">
                <input type="hidden" name="action" value="updateInfo">
                <input type="hidden" id="avatarInput" name="avatar" value="<c:out value='${user.avatar}'/>">

                <div class="avatar-row">
                    <c:choose>
                        <c:when test="${not empty user.avatarUrl}">
                            <img id="avatarDisplay" class="avatar-big" src="<c:out value='${user.avatar}'/>" alt="头像">
                        </c:when>
                        <c:otherwise>
                            <span id="avatarDisplay" class="m-ava avatar-big" style="background:${user.avatarColor}">
                                <c:out value="${user.avatarText}"/>
                            </span>
                        </c:otherwise>
                    </c:choose>
                    <div>
                        <input type="file" id="avatarFile" accept="image/*" hidden>
                        <button type="button" class="m-btn primary sm" id="avatarBtn">
                            <svg class="i"><use href="#i-box"></use></svg>上传更换头像
                        </button>
                        <p style="font-size:0.78rem;color:var(--m-text-muted);margin-top:6px" id="avatarTip">
                            支持 jpg / png / gif / webp，≤5MB，上传后点「保存资料」生效
                        </p>
                    </div>
                </div>

                <div class="m-field">
                    <label class="m-label">登录账号（不可修改）</label>
                    <input class="m-input" value="<c:out value='${user.username}'/>" disabled>
                </div>

                <div class="m-field">
                    <label class="m-label" for="nickname">论坛昵称</label>
                    <input class="m-input" id="nickname" name="nickname" maxlength="20"
                           value="<c:out value='${user.nickname}'/>" placeholder="展示给大家看的名字">
                </div>

                <div style="display:flex;gap:10px;align-items:center">
                    <button class="m-btn primary" type="submit">保存资料</button>
                    <a class="m-btn ghost" href="${ctx}/posts">返回论坛</a>
                </div>
            </form>
        </div>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-tool"></use></svg>修改登录密码</h4>
            <form action="${ctx}/user/profile" method="post">
                <input type="hidden" name="action" value="updatePwd">
                <div class="m-field">
                    <label class="m-label" for="oldPwd">原密码</label>
                    <input class="m-input" id="oldPwd" type="password" name="oldPwd" placeholder="输入当前密码">
                </div>
                <div class="m-field">
                    <label class="m-label" for="newPwd">新密码</label>
                    <input class="m-input" id="newPwd" type="password" name="newPwd" placeholder="6~20 位">
                </div>
                <div class="m-field">
                    <label class="m-label" for="confirmPwd">确认新密码</label>
                    <input class="m-input" id="confirmPwd" type="password" name="confirmPwd" placeholder="再输入一次">
                </div>
                <button class="m-btn primary" type="submit">修改密码</button>
            </form>
        </div>
    </section>

    <aside>
        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-doc"></use></svg>我的数据</h4>
            <div class="m-stats">
                <div class="m-stat"><b>${myPosts != null ? myPosts : 0}</b><span>我的帖子</span></div>
                <div class="m-stat"><b>${myReplies != null ? myReplies : 0}</b><span>我的回复</span></div>
            </div>
            <p style="font-size:0.78rem;color:var(--m-text-muted);margin-top:0.7rem">
                账号类型：<c:out value="${user.admin ? '管理员' : '普通用户'}"/>
            </p>
        </div>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-warn"></use></svg>账号安全</h4>
            <ul class="m-notice">
                <li>建议定期更换密码</li>
                <li>不要与他人共用账号</li>
                <li>头像请上传合规图片</li>
            </ul>
        </div>
    </aside>
</main>

<script>
    // 头像上传：复用 /upload 接口，拿到地址后回写隐藏域
    var fileInput = document.getElementById('avatarFile');
    var tip = document.getElementById('avatarTip');
    var display = document.getElementById('avatarDisplay');
    var hidden = document.getElementById('avatarInput');

    document.getElementById('avatarBtn').onclick = function () { fileInput.click(); };
    fileInput.onchange = function () {
        if (!fileInput.files || !fileInput.files[0]) { return; }
        var f = fileInput.files[0];
        tip.textContent = '正在上传：' + f.name + ' …';
        tip.style.color = 'var(--m-primary)';
        var fd = new FormData();
        fd.append('file', f);
        fetch('${ctx}/upload', { method: 'POST', body: fd })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                if (d.success) {
                    hidden.value = d.url;
                    if (display.tagName === 'IMG') {
                        display.src = d.url;
                    } else {
                        var img = document.createElement('img');
                        img.id = 'avatarDisplay';
                        img.className = 'avatar-big';
                        img.src = d.url;
                        img.alt = '头像';
                        display.parentNode.replaceChild(img, display);
                        display = img;
                    }
                    tip.textContent = '已上传，点「保存资料」生效';
                    tip.style.color = '#16a34a';
                } else {
                    tip.textContent = '上传失败：' + (d.message || '未知错误');
                    tip.style.color = '#dc2626';
                }
                fileInput.value = '';
            })
            .catch(function () {
                tip.textContent = '上传失败，请检查网络';
                tip.style.color = '#dc2626';
                fileInput.value = '';
            });
    };
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
