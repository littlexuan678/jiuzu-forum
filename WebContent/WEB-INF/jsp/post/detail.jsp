<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 帖子详情页 · 现代版 =====================
     数据由 PostDetailServlet 提供：post（帖子）、replies（回复列表）
     保留功能：点赞、编辑、删除、回复（带图上传）、AI 帮写、回复删除
--%>

<main class="m-main">
    <%-- ========== 左侧：正文 + 回复 ========== --%>
    <section>
        <c:if test="${not empty sessionScope.flash}">
            <div class="m-alert ok"><c:out value="${sessionScope.flash}"/></div>
            <c:remove var="flash" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="m-alert error"><c:out value="${sessionScope.flashError}"/></div>
            <c:remove var="flashError" scope="session"/>
        </c:if>

        <%-- 帖子正文 --%>
        <article class="m-card">
            <div class="m-detail-head">
                <div style="margin-bottom:0.4rem">
                    <c:if test="${post.top}"><span class="m-tag pin">置顶</span></c:if>
                    <span class="m-tag board"><c:out value="${post.boardName}"/></span>
                </div>
                <h1 class="m-detail-title"><c:out value="${post.title}"/></h1>
                <div class="m-meta">
                    <span class="who">
                        <span class="m-ava" style="background:${post.avatarColor}">
                            <c:out value="${post.nickname.substring(0,1)}"/>
                        </span>
                        <c:out value="${post.nickname}"/>
                    </span>
                    <span><c:out value="${post.createdText}"/></span>
                    <c:if test="${not empty post.ipDisplay}">
                        <span class="m-ip" title="IP 属地">📍<c:out value="${post.ipDisplay}"/></span>
                    </c:if>
                    <span><svg class="i"><use href="#i-eye"></use></svg><c:out value="${post.views}"/> 次浏览</span>
                    <span><svg class="i"><use href="#i-chat"></use></svg><c:out value="${post.replies}"/> 条回复</span>
                </div>
            </div>

            <%-- 1. 封面图：置顶在正文上方，横向撑满 --%>
            <c:if test="${not empty post.imagePath}">
                <div class="m-detail-cover">
                    <img src="${ctx}${post.imagePath}" alt="帖子封面" data-zoom loading="lazy">
                </div>
            </c:if>

            <%-- 2. 正文：由 MarkupUtil 渲染（先转义再放行图片/加粗/代码等轻量语法）--%>
            <%-- 2. 正文：由 MarkupUtil 渲染（先转义再放行图片/加粗/代码等轻量语法）--%>
            <%-- id=postBody 供“超长正文折叠”脚本检测高度 --%>
            <div id="postBody" class="m-detail-body">${post.contentHtml}</div>

            <%-- 超长正文：超过阈值由 JS 自动折叠，底部出现「展开阅读全文」--%>
            <div class="m-readmore" id="readMoreWrap" style="display:none">
                <button type="button" id="readMoreBtn" onclick="toggleReadMore()">
                    <span>👇 展开阅读全文</span>
                </button>
            </div>

            <%-- 3. 附件下载卡片：带类型图标、文件名与大小 --%>
            <c:if test="${post.hasAttachment}">
                <div class="m-attach">
                    <span class="m-attach-ic"><c:out value="${post.attachmentIcon}"/></span>
                    <div class="m-attach-info">
                        <div class="m-attach-name"><c:out value="${post.attachmentName}"/></div>
                        <div class="m-attach-meta">
                            附件
                            <c:if test="${not empty post.attachmentSize}">
                                · <c:out value="${post.attachmentSize}"/>
                            </c:if>
                        </div>
                    </div>
                    <a class="m-btn primary sm" href="${ctx}${post.attachmentDownload}">
                        <svg class="i"><use href="#i-down"></use></svg>下载
                    </a>
                </div>
            </c:if>

            <div class="m-action-bar">
                <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
                    <c:choose>
                        <c:when test="${empty sessionScope.user}">
                            <a class="m-like" href="${ctx}/login">
                                <svg class="i"><use href="#i-heart-o"></use></svg>赞 <c:out value="${post.likes}"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="m-like ${post.liked ? 'on' : ''}" href="${ctx}/like?id=${post.id}">
                                <svg class="i"><use href="${post.liked ? '#i-heart' : '#i-heart-o'}"></use></svg>
                                ${post.liked ? '已赞' : '赞'} <c:out value="${post.likes}"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
                    <a class="m-btn ghost sm" href="${ctx}/posts?boardId=${post.boardId}">返回板块</a>
                    <a class="m-btn ghost sm" href="${ctx}/posts">全部帖子</a>
                </div>

                <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq post.userId or sessionScope.user.admin)}">
                    <div style="display:flex;gap:8px;align-items:center">
                        <a class="m-btn ghost sm" href="${ctx}/post/edit?id=${post.id}"><svg class="i"><use href="#i-pen"></use></svg>编辑</a>
                        <form action="${ctx}/post/delete" method="post"
                              onsubmit="return confirm('确定删除这个帖子？回复也会一起删除')">
                            <input type="hidden" name="id" value="${post.id}">
                            <button class="m-btn danger sm" type="submit">删除</button>
                        </form>
                    </div>
                </c:if>
            </div>
        </article>

        <%-- 回复列表 --%>
        <div class="m-card">
            <h4 class="m-widget-title" style="border-bottom:1px solid var(--m-border-light);padding-bottom:0.5rem">
                全部回复 <span style="font-size:0.8rem;color:var(--m-text-muted);font-weight:400">（<c:out value="${empty replyTotal ? 0 : replyTotal}"/>）</span>
            </h4>

            <c:choose>
                <c:when test="${empty replies}">
                    <div class="m-empty">还没有人回复，来抢沙发</div>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${replies}" var="r" varStatus="vs">
                        <div class="m-reply">
                            <span class="m-ava" style="background:${r.avatarColor}">
                                <c:out value="${r.nickname.substring(0,1)}"/>
                            </span>
                            <div style="flex:1;min-width:0">
                                <div class="m-reply-head">
                                    <div>
                                        <strong style="color:var(--m-text)"><c:out value="${r.nickname}"/></strong>
                                        <span style="margin-left:8px"><c:out value="${r.createdText}"/></span>
                                        <c:if test="${not empty r.ipDisplay}">
                                            <span class="m-ip" title="IP 属地">📍<c:out value="${r.ipDisplay}"/></span>
                                        </c:if>
                                    </div>
                                    <div style="display:flex;align-items:center;gap:8px">
                                        <span class="m-floor">#${vs.count} 楼</span>
                                        <c:if test="${not empty sessionScope.user}">
                                            <button type="button" class="m-reply-btn"
                                                    onclick="toggleSubBox(${r.id}, this)">💬 回复</button>
                                        </c:if>
                                        <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq r.userId or sessionScope.user.admin)}">
                                            <form action="${ctx}/reply/delete" method="post"
                                                  onsubmit="return confirm('确定删除这条回复？楼中楼也会一起删除')">
                                                <input type="hidden" name="id" value="${r.id}">
                                                <button class="m-rm" type="submit">删除</button>
                                            </form>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="m-reply-body">${r.contentHtml}</div>
                                <c:if test="${not empty r.imagePath}">
                                    <div class="md-figure">
                                        <img class="m-reply-img" src="${ctx}${r.imagePath}" alt="回复配图" data-zoom loading="lazy">
                                    </div>
                                </c:if>

                                <%-- 楼中楼：该楼层下的子回复 --%>
                                <c:if test="${r.hasSub}">
                                    <div class="m-sub-list">
                                        <c:forEach items="${r.subReplies}" var="sub">
                                            <div class="m-sub">
                                                <span class="m-ava sm" style="background:${sub.avatarColor}">
                                                    <c:out value="${sub.nickname.substring(0,1)}"/>
                                                </span>
                                                <div class="m-sub-main">
                                                    <div class="m-sub-head">
                                                        <strong><c:out value="${sub.nickname}"/></strong>
                                                        <c:if test="${not empty sub.replyToUser}">
                                                            <span class="m-sub-at">回复</span>
                                                            <span class="m-sub-at-name">@<c:out value="${sub.replyToUser}"/></span>
                                                        </c:if>
                                                        <span class="m-sub-time"><c:out value="${sub.createdText}"/></span>
                                                        <c:if test="${not empty sub.ipDisplay}">
                                                            <span class="m-ip" title="IP 属地">📍<c:out value="${sub.ipDisplay}"/></span>
                                                        </c:if>
                                                        <span style="flex:1"></span>
                                                        <c:if test="${not empty sessionScope.user and (sessionScope.user.id eq sub.userId or sessionScope.user.admin)}">
                                                            <form action="${ctx}/reply/delete" method="post"
                                                                  onsubmit="return confirm('确定删除这条回复？')">
                                                                <input type="hidden" name="id" value="${sub.id}">
                                                                <button class="m-rm" type="submit">删除</button>
                                                            </form>
                                                        </c:if>
                                                    </div>
                                                    <div class="m-reply-body">${sub.contentHtml}</div>
                                                    <c:if test="${not empty sub.imagePath}">
                                                        <div class="md-figure">
                                                            <img class="m-reply-img" src="${ctx}${sub.imagePath}" alt="回复配图" data-zoom loading="lazy">
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:if>

                                <%-- 楼中楼快捷输入框：默认隐藏 --%>
                                <c:if test="${not empty sessionScope.user}">
                                    <div class="m-sub-box" id="subBox-${r.id}" style="display:none">
                                        <form action="${ctx}/reply/add" method="post" id="subForm-${r.id}">
                                            <input type="hidden" name="postId" value="${post.id}">
                                            <input type="hidden" name="parentId" value="${r.id}">
                                            <textarea class="m-textarea" name="content" rows="2"
                                                      placeholder="回复 @<c:out value='${r.nickname}'/>…" required
                                                      style="min-height:64px"></textarea>
                                            <div class="m-sub-foot">
                                                <span style="font-size:0.75rem;color:var(--m-text-muted)">Ctrl / ⌘ + Enter 发送</span>
                                                <span style="flex:1"></span>
                                                <button type="button" class="m-btn ghost sm" onclick="toggleSubBox(${r.id}, this)">取消</button>
                                                <button type="submit" class="m-btn primary sm">发送</button>
                                            </div>
                                        </form>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- 发表回复 --%>
        <div class="m-card">
            <h4 class="m-widget-title">参与讨论</h4>
            <c:choose>
                <c:when test="${empty sessionScope.user}">
                    <div class="m-empty">
                        <a class="m-btn primary" href="${ctx}/login">登录后参与回复</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <form action="${ctx}/reply/add" method="post" enctype="multipart/form-data" id="replyForm">
                        <input type="hidden" name="postId" value="${post.id}">
                        <div class="m-field">
                            <textarea class="m-textarea" id="replyContent" name="content"
                                      placeholder="写下你的看法…（也可以只发一张图）"></textarea>
                        </div>

                        <div class="m-img-pick">
                            <input type="file" id="replyImage" name="image" accept="image/*" hidden>
                            <button type="button" class="m-btn ghost sm" id="replyImgBtn">
                                <svg class="i"><use href="#i-box"></use></svg>配图
                            </button>
                            <span style="font-size:0.78rem;color:var(--m-text-muted)" id="replyImgName">支持 jpg / png / gif / webp，≤5MB</span>
                        </div>
                        <div class="m-img-prev" id="replyImgPrev" style="display:none">
                            <img id="replyImgThumb" src="" alt="预览">
                            <button type="button" class="m-rm" id="replyImgRm">移除</button>
                        </div>

                        <div style="margin-top:0.7rem">
                            <button type="button" class="m-btn ghost sm" id="aiReplyBtn">
                                <svg class="i"><use href="#i-spark"></use></svg>AI 帮写回复
                            </button>
                        </div>
                        <div class="m-ai-panel" id="aiReplyPanel" style="display:none">
                            <textarea class="m-textarea" id="aiReplyPrompt" placeholder="说说你想回什么，例如：同意楼主，补充一下周末可能下雨，记得带伞" style="min-height:70px"></textarea>
                            <div class="m-ai-foot">
                                <span style="font-size:0.78rem;color:var(--m-text-muted)" id="aiReplyHint"></span>
                                <span style="flex:1"></span>
                                <button type="button" class="m-btn ghost sm" id="aiReplyClose">收起</button>
                                <button type="button" class="m-btn primary sm" id="aiReplyGo">生成草稿</button>
                            </div>
                        </div>

                        <div style="display:flex;align-items:center;gap:10px;margin-top:0.9rem">
                            <span style="font-size:0.75rem;color:var(--m-text-muted)">Ctrl / ⌘ + Enter 发送</span>
                            <span style="flex:1"></span>
                            <button class="m-btn primary" type="submit">发表回复</button>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <%-- ========== 右侧：作者与公约 ========== --%>
    <aside>
        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-users"></use></svg>关于作者</h4>
            <p style="font-size:0.88rem;color:var(--m-text-sub)">楼主：<strong><c:out value="${post.nickname}"/></strong></p>
            <p style="font-size:0.8rem;color:var(--m-text-muted);margin-top:0.4rem">九组论坛活跃成员</p>
        </div>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-note"></use></svg>讨论公约</h4>
            <ul class="m-notice">
                <li>文明发帖，理性探讨</li>
                <li>鼓励分享具体代码或实践方案</li>
                <li>支持利用论坛 AI 助手辅助查证</li>
            </ul>
        </div>

        <div class="m-card">
            <h4 class="m-widget-title"><svg class="i"><use href="#i-spark"></use></svg>AI 助教</h4>
            <p style="font-size:0.83rem;color:var(--m-text-sub);margin-bottom:0.8rem">卡壳时可以让 AI 先起个草稿。</p>
            <a class="m-btn primary block" href="${ctx}/ai">打开 AI 助手</a>
        </div>
    </aside>
</main>

<script>
    // 图片点击放大
    document.addEventListener('click', function (e) {
        var img = e.target.closest ? e.target.closest('img[data-zoom]') : null;
        if (!img) { return; }
        var box = document.createElement('div');
        box.className = 'm-lightbox';
        box.innerHTML = '<img src="' + img.src + '" alt="放大">';
        box.onclick = function () { document.body.removeChild(box); };
        document.body.appendChild(box);
    });

    // Ctrl / ⌘ + Enter 提交回复（主回复框 + 所有楼中楼框）
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
            var el = e.target;
            if (el && el.tagName === 'TEXTAREA' && el.form) {
                e.preventDefault();
                el.form.submit();
            }
        }
    });

    // 楼中楼：展开 / 收起某个楼层下的快捷回复框
    window.toggleSubBox = function (replyId, btn) {
        var box = document.getElementById('subBox-' + replyId);
        if (!box) { return; }
        var hidden = box.style.display === 'none' || box.style.display === '';
        // 先收起其它已展开的，避免页面被多个输入框撑得很长
        var all = document.querySelectorAll('.m-sub-box');
        for (var i = 0; i < all.length; i++) {
            if (all[i] !== box) { all[i].style.display = 'none'; }
        }
        box.style.display = hidden ? 'block' : 'none';
        if (hidden) {
            var ta = box.querySelector('textarea');
            if (ta) { ta.focus(); }
            if (btn && btn.scrollIntoView) {
                btn.scrollIntoView({block: 'center', behavior: 'smooth'});
            }
        }
    };

    // 回复配图：本地预览 + 移除
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
            reader.onload = function (ev) {
                thumb.src = ev.target.result;
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

    // AI 帮写回复
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

<script>
    // 超长正文：渲染后若真实高度超过阈值，自动折叠并露出「展开阅读全文」
    window.addEventListener('DOMContentLoaded', function () {
        var body = document.getElementById('postBody');
        var wrap = document.getElementById('readMoreWrap');
        if (body && wrap && body.scrollHeight > 560) {
            body.classList.add('m-detail-collapse');
            wrap.style.display = 'block';
        }
    });

    // 展开 / 收起切换
    function toggleReadMore() {
        var body = document.getElementById('postBody');
        var btn = document.getElementById('readMoreBtn');
        if (!body || !btn) { return; }
        var label = btn.querySelector('span');
        if (body.classList.contains('expanded')) {
            body.classList.remove('expanded');
            if (label) { label.textContent = '👇 展开阅读全文'; }
            body.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } else {
            body.classList.add('expanded');
            if (label) { label.textContent = '👆 收起长文'; }
        }
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
