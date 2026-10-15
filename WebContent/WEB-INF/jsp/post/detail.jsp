<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 帖子详情页 =====================
     数据由 PostDetailServlet 提供：post（帖子）、replies（回复列表） --%>

<div class="container" style="padding-top:92px">
    <%-- Session 中的操作反馈 --%>
    <c:if test="${not empty sessionScope.flash}">
        <div class="alert ok"><c:out value="${sessionScope.flash}"/></div>
        <c:remove var="flash" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert error"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <%-- 帖子正文 --%>
    <article class="detail card">
        <h1>
            <c:if test="${post.top}"><span class="tag-pin">置顶</span> </c:if>
            <c:out value="${post.title}"/>
        </h1>
        <div class="dm">
            <span class="who">
                <span class="ava" style="background:${post.avatarColor}">
                    <c:out value="${post.nickname.substring(0,1)}"/>
                </span>
                <c:out value="${post.nickname}"/>
            </span>
            <span><c:out value="${post.createdText}"/></span>
            <span><svg class="i"><use href="#i-eye"></use></svg><c:out value="${post.views}"/> 次浏览</span>
            <span class="tag-board ${post.boardName eq '公告区' ? 'admin' : ''}">
                <jsp:include page="/WEB-INF/jsp/common/board-icon.jsp"><jsp:param name="name" value="${post.boardName}"/></jsp:include>
                <c:out value="${post.boardName}"/>
            </span>
        </div>

        <div class="detail-body"><c:out value="${post.content}"/></div>

        <%-- 帖子配图 --%>
        <c:if test="${not empty post.imagePath}">
            <div class="post-media">
                <a href="${ctx}${post.imagePath}" target="_blank" title="点击查看大图">
                    <img src="${ctx}${post.imagePath}" alt="帖子配图" loading="lazy">
                </a>
            </div>
        </c:if>

        <div class="detail-acts">
            <%-- 点赞：已点赞显示“已赞” --%>
            <c:choose>
                <c:when test="${empty sessionScope.user}">
                    <a class="btn ghost" href="${ctx}/login?redirect=${pageContext.request.requestURI}%3Fid%3D${post.id}">
                        <svg class="i"><use href="#i-heart-o"></use></svg>赞 <c:out value="${post.likes}"/>
                    </a>
                </c:when>
                <c:otherwise>
                    <a class="btn ${post.liked ? '' : 'ghost'}" href="${ctx}/like?id=${post.id}">
                        <svg class="i"><use href="${post.liked ? '#i-heart' : '#i-heart-o'}"></use></svg>${post.liked ? '已赞' : '赞'} <c:out value="${post.likes}"/>
                    </a>
                </c:otherwise>
            </c:choose>

            <a class="btn ghost" href="${ctx}/posts?boardId=${post.boardId}">返回板块</a>
            <a class="btn ghost" href="${ctx}/posts">全部帖子</a>

            <%-- 作者本人或管理员可编辑 / 删除 --%>
            <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq post.userId or sessionScope.user.admin)}">
                <a class="btn ghost" href="${ctx}/post/edit?id=${post.id}" style="margin-left:auto">
                    <svg class="i"><use href="#i-pen"></use></svg>编辑
                </a>
                <form action="${ctx}/post/delete" method="post"
                      onsubmit="return confirm('确定删除这个帖子？回复也会一起删除')">
                    <input type="hidden" name="id" value="${post.id}">
                    <button class="btn danger" type="submit">删除帖子</button>
                </form>
            </c:if>
        </div>
    </article>

    <%-- 回复区 --%>
    <div class="reply-sec">
        <h4>全部回复（<c:out value="${empty replies ? 0 : replies.size()}"/>）</h4>
        <c:choose>
            <c:when test="${empty replies}">
                <div class="card empty" style="padding:30px"><svg class="i"><use href="#i-chat"></use></svg>还没有人回复，来抢沙发</div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${replies}" var="r">
                    <div class="reply card">
                        <div class="rm">
                            <span class="who">
                                <span class="ava xs" style="background:${r.avatarColor}">
                                    <c:out value="${r.nickname.substring(0,1)}"/>
                                </span>
                                <c:out value="${r.nickname}"/>
                            </span>
                            <span><c:out value="${r.createdText}"/></span>
                            <%-- 回复者本人或管理员可删除 --%>
                            <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq r.userId or sessionScope.user.admin)}">
                                <form action="${ctx}/reply/delete" method="post" style="margin-left:auto"
                                      onsubmit="return confirm('确定删除这条回复？')">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <button class="rdel" type="submit">删除</button>
                                </form>
                            </c:if>
                        </div>
                        <div class="rc"><c:out value="${r.content}"/></div>
                        <%-- 回复配图 --%>
                        <c:if test="${not empty r.imagePath}">
                            <div class="rc-media">
                                <a href="${ctx}${r.imagePath}" target="_blank" title="点击查看大图">
                                    <img src="${ctx}${r.imagePath}" alt="回复配图" loading="lazy">
                                </a>
                            </div>
                        </c:if>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 回复输入框 --%>
    <div class="reply-box card" style="margin-top:10px">
        <c:choose>
            <c:when test="${empty sessionScope.user}">
                <div class="empty" style="padding:20px">
                    <a class="btn" href="${ctx}/login">登录后参与回复</a>
                </div>
            </c:when>
            <c:otherwise>
                <form action="${ctx}/reply/add" method="post" enctype="multipart/form-data" id="replyForm">
                    <input type="hidden" name="postId" value="${post.id}">
                    <textarea class="textarea" id="replyContent" name="content" placeholder="写下你的看法…（也可以只发一张图）"></textarea>

                    <%-- 配图选择 + 本地预览 --%>
                    <div class="img-pick" style="margin-top:8px">
                        <input type="file" id="replyImage" name="image" accept="image/*" hidden>
                        <button type="button" class="btn ghost" id="replyImgBtn">
                            <svg class="i"><use href="#i-box"></use></svg>配图
                        </button>
                        <span class="muted" id="replyImgName" style="font-size:11.5px">支持 jpg / png / gif / webp，≤5MB</span>
                    </div>
                    <div class="img-prev" id="replyImgPrev" style="display:none">
                        <img id="replyImgThumb" src="" alt="预览">
                        <button type="button" class="rm-img" id="replyImgRm" title="移除">
                            <svg class="i"><use href="#i-x"></use></svg>移除
                        </button>
                    </div>

                    <div style="display:flex;gap:8px;align-items:center;margin-top:8px">
                        <button type="button" class="btn ghost" id="aiReplyBtn">
                            <svg class="i"><use href="#i-spark"></use></svg>AI 帮写回复
                        </button>
                        <span class="muted" style="font-size:11.5px">不知道怎么回？让 AI 给个草稿</span>
                    </div>
                    <div class="ai-compose" id="aiReplyPanel" style="display:none;margin-top:8px">
                        <textarea class="textarea" id="aiReplyPrompt" placeholder="说说你想回什么，例如：同意楼主，补充一下周末天气可能下雨，记得带伞"></textarea>
                        <div class="ai-compose-foot">
                            <span class="muted" id="aiReplyHint"></span>
                            <span style="flex:1"></span>
                            <button type="button" class="btn ghost" id="aiReplyClose">收起</button>
                            <button type="button" class="btn" id="aiReplyGo">生成草稿</button>
                        </div>
                    </div>
                    <div class="rb-foot">
                        <span class="tip">Ctrl / ⌘ + Enter 发送</span>
                        <button class="btn" type="submit">发表回复</button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
    // 回复框支持 Ctrl/⌘ + Enter 提交
    var ta = document.querySelector('.reply-box textarea');
    if (ta) {
        ta.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                e.preventDefault();
                ta.form.submit();
            }
        });
    }

    // 回复配图：选择后本地预览，可移除
    (function () {
        var file = document.getElementById('replyImage');
        var btn = document.getElementById('replyImgBtn');
        var nameEl = document.getElementById('replyImgName');
        var prev = document.getElementById('replyImgPrev');
        var thumb = document.getElementById('replyImgThumb');
        var rm = document.getElementById('replyImgRm');
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

    // 回复框的 AI 帮写
    (function () {
        var btn = document.getElementById('aiReplyBtn');
        var panel = document.getElementById('aiReplyPanel');
        var go = document.getElementById('aiReplyGo');
        var closeBtn = document.getElementById('aiReplyClose');
        var promptEl = document.getElementById('aiReplyPrompt');
        var hint = document.getElementById('aiReplyHint');
        var contentEl = document.getElementById('replyContent');
        if (!btn) { return; }
        btn.onclick = function () { panel.style.display = panel.style.display === 'none' ? 'block' : 'none'; };
        closeBtn.onclick = function () { panel.style.display = 'none'; };
        go.onclick = function () {
            var p = promptEl.value.trim();
            if (!p) { hint.textContent = '先描述一下你想回什么'; return; }
            go.disabled = true; go.textContent = '生成中…'; hint.textContent = 'AI 正在起稿…';
            fetch('${ctx}/ai/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ action: 'compose', kind: 'reply', prompt: p })
            })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                if (d.ok) {
                    var text = (d.reply || '').trim();
                    contentEl.value = (contentEl.value ? contentEl.value + '\n\n' : '') + text;
                    hint.textContent = '已填入草稿，可修改后再发表';
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

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
