<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 发帖 / 编辑 · 沉浸式编辑器 =====================
     说明：本页沿用站内公共头尾（header.jsp 已输出 html/head/body），
           因此这里不能再写 DOCTYPE 与 link，否则会出现嵌套 html。
     功能：板块选择、标题、工具栏（加粗/代码/代码块/引用/分割线/插入图片到光标处）、
           AI 帮写草稿、封面配图上传、右侧发帖技巧。
--%>

<main class="m-main editor-main">
    <section class="m-card editor-card">
        <div class="editor-head">
            <h2 class="editor-title">
                <svg class="i"><use href="#i-pen"></use></svg>
                ${editing ? '编辑帖子' : '创作新内容'}
            </h2>
            <c:choose>
                <c:when test="${editing}">
                    <a class="m-btn ghost sm" href="${ctx}/post/detail?id=${post.id}">放弃修改</a>
                </c:when>
                <c:otherwise>
                    <a class="m-btn ghost sm" href="${ctx}/posts">返回列表</a>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${not empty error}">
            <div class="m-alert error"><c:out value="${error}"/></div>
        </c:if>

        <form action="${ctx}${editing ? '/post/edit' : '/post/add'}" method="post" enctype="multipart/form-data">
            <c:if test="${editing}">
                <input type="hidden" name="id" value="${post.id}">
            </c:if>

            <div class="m-field">
                <label class="m-label" for="boardSel">选择板块</label>
                <select class="m-input" id="boardSel" name="boardId">
                    <c:forEach items="${boards}" var="b">
                        <option value="${b.id}" ${boardId eq b.id ? 'selected' : ''}>
                            <c:out value="${b.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="m-field">
                <label class="m-label" for="title">帖子标题</label>
                <input class="m-input" id="title" name="title" maxlength="60"
                       placeholder="输入引人注目的标题…" value="<c:out value='${post.title}'/>" autofocus>
            </div>

            <div class="m-field">
                <label class="m-label">正文内容（支持任意位置图文混排）</label>

                <%-- 快捷工具栏 --%>
                <div class="toolbar">
                    <input type="file" id="inlineImage" accept="image/*" hidden>
                    <button type="button" class="tool-btn primary" id="btnImg">
                        <svg class="i"><use href="#i-box"></use></svg>插入图片到光标处
                    </button>
                    <button type="button" class="tool-btn" data-prefix="**" data-suffix="**"><b>B</b> 加粗</button>
                    <button type="button" class="tool-btn" data-prefix="`" data-suffix="`">代码</button>
                    <button type="button" class="tool-btn" data-prefix="&#10;```&#10;" data-suffix="&#10;```&#10;">代码块</button>
                    <button type="button" class="tool-btn" data-prefix="&#10;&gt; " data-suffix="">引用</button>
                    <button type="button" class="tool-btn" data-prefix="&#10;---&#10;" data-suffix="">分割线</button>
                    <span class="tool-hint" id="uploadHint">光标点哪里，图就插哪里</span>
                </div>

                <textarea class="m-textarea editor-textarea" id="postContent" name="content"
                          placeholder="在此挥洒你的灵感…&#10;&#10;小提示：把光标点到两段文字中间，再点上方「插入图片」，图片就会插到段落之间。"><c:out value='${post.content}'/></textarea>

                <div style="margin-top:0.6rem">
                    <button type="button" class="m-btn ghost sm" id="aiComposeBtn">
                        <svg class="i"><use href="#i-spark"></use></svg>AI 帮写
                    </button>
                </div>
                <div class="m-ai-panel" id="aiComposePanel" style="display:none">
                    <textarea class="m-textarea" id="aiPrompt" style="min-height:70px"
                              placeholder="描述你想发的帖子，例如：想发个帖问问大家周末去哪玩，顺便组队"></textarea>
                    <div class="m-ai-foot">
                        <span style="font-size:0.78rem;color:var(--m-text-muted)" id="aiHint"></span>
                        <span style="flex:1"></span>
                        <button type="button" class="m-btn ghost sm" id="aiComposeClose">收起</button>
                        <button type="button" class="m-btn primary sm" id="aiComposeGo">生成草稿</button>
                    </div>
                </div>
            </div>

            <div class="m-field">
                <label class="m-label">封面配图（可选，单独显示在正文上方）</label>
                <c:if test="${not empty post.imagePath}">
                    <div class="m-img-prev" style="display:flex">
                        <img src="${ctx}${post.imagePath}" alt="当前配图">
                        <label class="m-rm" style="display:inline-flex;align-items:center;gap:4px">
                            <input type="checkbox" name="removeImage" value="1"> 移除这张配图
                        </label>
                    </div>
                </c:if>
                <div class="m-img-pick">
                    <input type="file" id="postImage" name="image" accept="image/*" hidden>
                    <button type="button" class="m-btn ghost sm" id="postImgBtn">
                        <svg class="i"><use href="#i-box"></use></svg>选择封面
                    </button>
                    <span style="font-size:0.78rem;color:var(--m-text-muted)" id="postImgName">支持 jpg / png / gif / webp，≤5MB</span>
                </div>
                <div class="m-img-prev" id="postImgPrev" style="display:none">
                    <img id="postImgThumb" src="" alt="预览">
                    <button type="button" class="m-rm" id="postImgRm">移除</button>
                </div>
            </div>

            <div class="m-field">
                <label class="m-label">📎 帖子附件（可选，用于分享源码压缩包 / 实验报告等）</label>
                <c:choose>
                    <c:when test="${editing and post.hasAttachment}">
                        <div class="m-attach" style="margin:0 0 0.6rem">
                            <span class="m-attach-ic"><c:out value="${post.attachmentIcon}"/></span>
                            <div class="m-attach-info">
                                <div class="m-attach-name"><c:out value="${post.attachmentName}"/></div>
                                <div class="m-attach-meta">当前附件<c:if test="${not empty post.attachmentSize}"> · <c:out value="${post.attachmentSize}"/></c:if></div>
                            </div>
                            <label class="m-rm" style="display:inline-flex;align-items:center;gap:4px">
                                <input type="checkbox" name="removeAttachment" value="1"> 移除
                            </label>
                        </div>
                    </c:when>
                </c:choose>
                <div class="m-img-pick">
                    <input type="file" id="attachInput" hidden>
                    <button type="button" class="m-btn ghost sm" id="attachBtn">
                        <svg class="i"><use href="#i-box"></use></svg>选择本地文件
                    </button>
                    <span style="font-size:0.78rem;color:var(--m-text-muted)" id="attachName">
                        支持 zip / rar / 7z / pdf / docx / xlsx / pptx / txt 等，≤20MB
                    </span>
                </div>
                <div class="m-attach-picked" id="attachPicked" style="display:none">
                    <span id="attachPickedName"></span>
                    <button type="button" class="m-rm" id="attachRm">移除</button>
                </div>
                <input type="hidden" name="attachmentUrl" id="attachmentUrl" value="${post.attachmentUrl}">
                <input type="hidden" name="attachmentName" id="attachmentName" value="${post.attachmentName}">
                <input type="hidden" name="attachmentSize" id="attachmentSize" value="${post.attachmentSize}">
            </div>

            <div style="display:flex;justify-content:flex-end;gap:10px">
                <a class="m-btn ghost" href="${ctx}/posts">取消</a>
                <button class="m-btn primary" type="submit">${editing ? '保存修改' : '立即发布'}</button>
            </div>
        </form>
    </section>

    <%-- 右侧发帖技巧 --%>
    <aside>
        <div class="m-card tips-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-note"></use></svg>发帖小技巧</h4>
            <ul class="m-notice">
                <li><strong>图文排版：</strong>把光标点到两段文字中间，点「插入图片」，图片会自动插到段落之间。</li>
                <li><strong>支持多图：</strong>不限制数量，多次点选即可插入多张照片。</li>
                <li><strong>代码展示：</strong>讨论编程题时用「代码块」包裹，排版更清晰。</li>
                <li><strong>封面配图：</strong>会单独显示在正文上方，适合放题图。</li>
                <li><strong>AI 助手：</strong>卡壳时点「AI 帮写」先起个草稿，再自己改。</li>
            </ul>
        </div>

        <div class="m-card tips-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-warn"></use></svg>注意事项</h4>
            <ul class="m-notice">
                <li>标题最多 60 字</li>
                <li>正文和封面图不能同时为空</li>
                <li>文明发言，理性讨论</li>
            </ul>
        </div>
    </aside>
</main>

<script>
    var ta = document.getElementById('postContent');
    var lastPos = 0;

    function saveCursor() { lastPos = ta.selectionStart; }
    ta.addEventListener('keyup', saveCursor);
    ta.addEventListener('click', saveCursor);
    ta.addEventListener('select', saveCursor);

    // 工具栏：加粗 / 代码 / 代码块 / 引用 / 分割线
    var tools = document.querySelectorAll('.tool-btn[data-prefix]');
    for (var i = 0; i < tools.length; i++) {
        tools[i].onclick = function () {
            var pre = this.getAttribute('data-prefix'), suf = this.getAttribute('data-suffix') || '';
            var s = ta.selectionStart, e = ta.selectionEnd, v = ta.value;
            var sel = v.substring(s, e);
            ta.value = v.substring(0, s) + pre + sel + suf + v.substring(e);
            ta.focus();
            ta.setSelectionRange(s + pre.length, e + pre.length);
            saveCursor();
        };
    }

    // 插入图片：上传到 /upload，拿到地址后以 Markdown 形式插到光标处
    var fileInput = document.getElementById('inlineImage');
    var hint = document.getElementById('uploadHint');
    document.getElementById('btnImg').onclick = function () { fileInput.click(); };
    fileInput.onchange = function () {
        if (!fileInput.files || !fileInput.files[0]) { return; }
        var f = fileInput.files[0];
        hint.textContent = '正在上传：' + f.name + ' …';
        hint.style.color = 'var(--m-primary)';
        var fd = new FormData();
        fd.append('file', f);
        fetch('${ctx}/upload', { method: 'POST', body: fd })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                if (d.success) {
                    var pos = lastPos;
                    var tag = '\n![图片](' + d.url + ')\n';
                    ta.value = ta.value.substring(0, pos) + tag + ta.value.substring(pos);
                    var np = pos + tag.length;
                    ta.focus();
                    ta.setSelectionRange(np, np);
                    lastPos = np;
                    hint.textContent = '已插入到光标处！';
                    hint.style.color = '#16a34a';
                } else {
                    hint.textContent = '上传失败：' + (d.message || '未知错误');
                    hint.style.color = '#dc2626';
                }
                fileInput.value = '';
            })
            .catch(function () {
                hint.textContent = '上传失败，请检查网络';
                hint.style.color = '#dc2626';
                fileInput.value = '';
            });
    };

    // 封面配图预览
    (function () {
        var file = document.getElementById('postImage');
        var btn = document.getElementById('postImgBtn');
        var nameEl = document.getElementById('postImgName');
        var prev = document.getElementById('postImgPrev');
        var thumb = document.getElementById('postImgThumb');
        var rm = document.getElementById('postImgRm');
        btn.onclick = function () { file.click(); };
        file.onchange = function () {
            if (!file.files || !file.files[0]) { return; }
            var f = file.files[0];
            nameEl.textContent = f.name + '（' + (f.size / 1024 / 1024).toFixed(2) + 'MB）';
            var reader = new FileReader();
            reader.onload = function (e) { thumb.src = e.target.result; prev.style.display = 'flex'; };
            reader.readAsDataURL(f);
        };
        rm.onclick = function () {
            file.value = '';
            prev.style.display = 'none';
            thumb.src = '';
            nameEl.textContent = '支持 jpg / png / gif / webp，≤5MB';
        };
    })();

    // 附件上传：走 /upload?type=file，成功后把 url / 文件名 / 大小写进隐藏域
    (function () {
        var input = document.getElementById('attachInput');
        var btn = document.getElementById('attachBtn');
        var nameEl = document.getElementById('attachName');
        var picked = document.getElementById('attachPicked');
        var pickedName = document.getElementById('attachPickedName');
        var rm = document.getElementById('attachRm');
        var urlEl = document.getElementById('attachmentUrl');
        var nmEl = document.getElementById('attachmentName');
        var szEl = document.getElementById('attachmentSize');
        if (!input) { return; }

        btn.onclick = function () { input.click(); };
        input.onchange = function () {
            if (!input.files || !input.files[0]) { return; }
            var f = input.files[0];
            nameEl.textContent = '正在上传：' + f.name + ' …';
            nameEl.style.color = 'var(--m-primary)';
            var fd = new FormData();
            fd.append('file', f);
            fetch('${ctx}/upload?type=file', { method: 'POST', body: fd })
                .then(function (r) { return r.json(); })
                .then(function (d) {
                    if (d.success) {
                        urlEl.value = d.url;
                        nmEl.value = d.fileName || f.name;
                        szEl.value = d.fileSize || '';
                        pickedName.textContent = (d.fileName || f.name) + (d.fileSize ? '（' + d.fileSize + '）' : '');
                        picked.style.display = 'flex';
                        nameEl.textContent = '已就绪，发布后可在文末下载';
                        nameEl.style.color = '#16a34a';
                    } else {
                        nameEl.textContent = '上传失败：' + (d.message || '未知错误');
                        nameEl.style.color = '#dc2626';
                    }
                    input.value = '';
                })
                .catch(function () {
                    nameEl.textContent = '上传失败，请检查网络';
                    nameEl.style.color = '#dc2626';
                    input.value = '';
                });
        };
        rm.onclick = function () {
            urlEl.value = '';
            nmEl.value = '';
            szEl.value = '';
            picked.style.display = 'none';
            pickedName.textContent = '';
            nameEl.textContent = '支持 zip / rar / 7z / pdf / docx / xlsx / pptx / txt 等，≤20MB';
            nameEl.style.color = 'var(--m-text-muted)';
        };
    })();

    // AI 帮写
    (function () {
        var btn = document.getElementById('aiComposeBtn');
        var panel = document.getElementById('aiComposePanel');
        var go = document.getElementById('aiComposeGo');
        var closeBtn = document.getElementById('aiComposeClose');
        var promptEl = document.getElementById('aiPrompt');
        var aiHint = document.getElementById('aiHint');
        var titleEl = document.getElementById('title');
        btn.onclick = function () { panel.style.display = panel.style.display === 'none' ? 'block' : 'none'; };
        closeBtn.onclick = function () { panel.style.display = 'none'; };
        go.onclick = function () {
            var p = promptEl.value.trim();
            if (!p) { aiHint.textContent = '先描述一下你想发什么'; return; }
            go.disabled = true; go.textContent = '生成中…'; aiHint.textContent = 'AI 正在起稿…';
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
                        ta.value = (ta.value ? ta.value + '\n\n' : '') + mt[2].trim();
                    } else {
                        ta.value = (ta.value ? ta.value + '\n\n' : '') + text;
                    }
                    aiHint.textContent = '已填入草稿，可修改后再发布';
                    panel.style.display = 'none';
                } else {
                    aiHint.textContent = d.error || '生成失败';
                }
            })
            .catch(function () { aiHint.textContent = '网络错误，请重试'; })
            .finally(function () { go.disabled = false; go.textContent = '生成草稿'; });
        };
    })();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
