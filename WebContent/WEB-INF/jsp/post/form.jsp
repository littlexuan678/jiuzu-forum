<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 发帖表单 =====================
     GET /post/add 由 PostAddServlet 提供板块列表；POST 提交到同一地址 --%>

<div class="container" style="padding-top:92px;max-width:760px">
    <div class="detail card">
        <h1 style="font-size:19px;margin-bottom:14px">
            <svg class="i"><use href="#i-pen"></use></svg>${editing ? '编辑帖子' : '发布新帖'}
        </h1>

        <c:if test="${not empty error}">
            <div class="alert error"><c:out value="${error}"/></div>
        </c:if>

        <form action="${ctx}${editing ? '/post/edit' : '/post/add'}" method="post" enctype="multipart/form-data">
            <c:if test="${editing}">
                <input type="hidden" name="id" value="${post.id}">
            </c:if>
            <div class="field">
                <label for="boardSel">选择板块</label>
                <select class="select" id="boardSel" name="boardId">
                    <c:forEach items="${boards}" var="b">
                        <option value="${b.id}" ${boardId eq b.id ? 'selected' : ''}>
                            <c:out value="${b.name}"/>
                            <c:if test="${not empty b.description}"> — <c:out value="${b.description}"/></c:if>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="title">标题</label>
                <input class="input" id="title" name="title" value="<c:out value='${post.title}'/>"
                       placeholder="一句话说清你的帖子" maxlength="60" autofocus>
            </div>
            <div class="field">
                <label for="content">正文</label>
                <textarea class="textarea" id="content" name="content" placeholder="写点什么…（支持换行）"><c:out value='${post.content}'/></textarea>
                <div style="display:flex;gap:8px;align-items:center;margin-top:8px">
                    <button type="button" class="btn ghost" id="aiComposeBtn">
                        <svg class="i"><use href="#i-spark"></use></svg>AI 帮写
                    </button>
                    <span class="muted" style="font-size:11.5px">卡壳了？让 AI 给你起个草稿</span>
                </div>
                <div class="ai-compose" id="aiComposePanel" style="display:none">
                    <textarea class="textarea" id="aiPrompt" placeholder="描述你想发的帖子，例如：想发个帖问问大家周末去哪玩，顺便组队"></textarea>
                    <div class="ai-compose-foot">
                        <span class="muted" id="aiHint"></span>
                        <span style="flex:1"></span>
                        <button type="button" class="btn ghost" id="aiComposeClose">收起</button>
                        <button type="button" class="btn" id="aiComposeGo">生成草稿</button>
                    </div>
                </div>
            </div>
            <div class="field">
                <label>${editing and not empty post.imagePath ? '更换配图' : '配图（可选）'}</label>

                <%-- 编辑已有帖子时展示当前配图，可勾选移除 --%>
                <c:if test="${editing and not empty post.imagePath}">
                    <div class="img-prev" style="display:flex">
                        <img src="${ctx}${post.imagePath}" alt="当前配图">
                        <label class="rm-img" style="margin:0;display:inline-flex;align-items:center">
                            <input type="checkbox" name="removeImage" value="1"> 移除这张配图
                        </label>
                    </div>
                </c:if>

                <div class="img-pick">
                    <input type="file" id="postImage" name="image" accept="image/*" hidden>
                    <button type="button" class="btn ghost" id="postImgBtn">
                        <svg class="i"><use href="#i-box"></use></svg>选择图片
                    </button>
                    <span class="muted" id="postImgName" style="font-size:11.5px">支持 jpg / png / gif / webp，≤5MB</span>
                </div>
                <div class="img-prev" id="postImgPrev" style="display:none">
                    <img id="postImgThumb" src="" alt="预览">
                    <button type="button" class="rm-img" id="postImgRm" title="移除">
                        <svg class="i"><use href="#i-x"></use></svg>移除
                    </button>
                </div>
            </div>
            <div style="display:flex;gap:8px;align-items:center">
                <button class="btn" type="submit">${editing ? '保存修改' : '发布帖子'}</button>
                <c:choose>
                    <c:when test="${editing}">
                        <a class="btn ghost" href="${ctx}/post/detail?id=${post.id}">放弃修改</a>
                    </c:when>
                    <c:otherwise>
                        <a class="btn ghost" href="${ctx}/posts">取消</a>
                    </c:otherwise>
                </c:choose>
                <span class="muted" style="font-size:11.5px;margin-left:auto">标题最多 60 字</span>
            </div>
        </form>

        <script>
            // 配图：选择后本地预览，可移除
            (function () {
                var file = document.getElementById('postImage');
                var btn = document.getElementById('postImgBtn');
                var nameEl = document.getElementById('postImgName');
                var prev = document.getElementById('postImgPrev');
                var thumb = document.getElementById('postImgThumb');
                var rm = document.getElementById('postImgRm');
                if (!file) { return; }
                btn.onclick = function () { file.click(); };
                file.onchange = function () {
                    if (!file.files || !file.files[0]) { return; }
                    var f = file.files[0];
                    nameEl.textContent = f.name + '（' + (f.size / 1024 / 1024).toFixed(2) + 'MB）';
                    var reader = new FileReader();
                    reader.onload = function (e) {
                        thumb.src = e.target.result;
                        prev.style.display = 'flex';
                    };
                    reader.readAsDataURL(f);
                };
                rm.onclick = function () {
                    file.value = '';
                    prev.style.display = 'none';
                    thumb.src = '';
                    nameEl.textContent = '支持 jpg / png / gif / webp，≤5MB';
                };
            })();

            (function () {
                var btn = document.getElementById('aiComposeBtn');
                var panel = document.getElementById('aiComposePanel');
                var go = document.getElementById('aiComposeGo');
                var closeBtn = document.getElementById('aiComposeClose');
                var promptEl = document.getElementById('aiPrompt');
                var hint = document.getElementById('aiHint');
                var titleEl = document.getElementById('title');
                var contentEl = document.getElementById('content');
                if (!btn) { return; }
                btn.onclick = function () { panel.style.display = panel.style.display === 'none' ? 'block' : 'none'; };
                closeBtn.onclick = function () { panel.style.display = 'none'; };
                go.onclick = function () {
                    var p = promptEl.value.trim();
                    if (!p) { hint.textContent = '先描述一下你想发什么'; return; }
                    go.disabled = true; go.textContent = '生成中…'; hint.textContent = 'AI 正在起稿…';
                    fetch('${ctx}/ai/chat', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ action: 'compose', kind: 'post', prompt: p })
                    })
                    .then(function (r) { return r.json(); })
                    .then(function (d) {
                        if (d.ok) {
                            var text = (d.reply || '').trim();
                            var mt = text.match(/【标题】\s*(.+?)\s*(?:\n|$)([\s\S]*)/);
                            if (mt) {
                                if (!titleEl.value.trim()) { titleEl.value = mt[1].trim(); }
                                contentEl.value = (contentEl.value ? contentEl.value + '\n\n' : '') + mt[2].trim();
                            } else {
                                contentEl.value = (contentEl.value ? contentEl.value + '\n\n' : '') + text;
                            }
                            hint.textContent = '已填入草稿，可修改后再发布';
                            panel.style.display = 'none';
                        } else {
                            hint.textContent = d.error || '生成失败';
                        }
                    })
                    .catch(function () { hint.textContent = '网络错误，请重试'; })
                    .finally(function () { go.disabled = false; go.textContent = '生成草稿'; });
                };
            })();
        </script>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
