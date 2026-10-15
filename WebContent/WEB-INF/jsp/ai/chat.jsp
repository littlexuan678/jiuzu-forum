<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
<%-- AI 助手：接入 DeepSeek 大模型的站内聊天页 --%>
<div class="container ai-page">
    <div class="ai-head card">
        <div class="ai-logo"><img src="${ctx}/static/images/logo.png" alt="小九"></div>
        <div class="ai-head-text">
            <h1>AI 助手 <b>小九</b></h1>
            <p>由 DeepSeek 大模型驱动 · 问答 / 闲聊 / 写文案，连续多轮对话</p>
        </div>
        <span class="ai-status ${aiReady ? 'on' : 'off'}">
            <i class="dot"></i>${aiReady ? '已连接' : '未配置'}
        </span>
        <button class="btn ghost sm" id="aiClear" type="button">
            <svg class="i"><use href="#i-trash"></use></svg>清空对话
        </button>
    </div>

    <c:choose>
        <c:when test="${not aiReady}">
            <div class="card empty" style="padding:48px 24px;text-align:center">
                <span class="big"><svg class="i" style="width:46px;height:46px;opacity:.5"><use href="#i-warn"/></svg></span>
                <b>AI 助手尚未配置</b>
                <p class="muted" style="margin-top:8px;max-width:420px;margin-left:auto;margin-right:auto">
                    请在服务器 <code>WEB-INF/config.properties</code> 中填入有效的 DeepSeek API Key
                    （<code>deepseek.api.key=sk-xxx</code>），重启应用后即可使用。
                </p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card ai-box">
                <div class="ai-msgs" id="aiMsgs">
                    <c:choose>
                        <c:when test="${empty history}">
                            <div class="ai-msg ai">
                                <span class="ai-ava"><img src="${ctx}/static/images/logo.png" alt="小九"></span>
                                <div class="ai-bubble">你好呀，我是小九！有什么想问的、想聊的、想让我帮忙写的，尽管说～</div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${history}" var="m">
                                <c:choose>
                                    <c:when test="${m[0] == 'user'}">
                                        <div class="ai-msg me"><div class="ai-bubble"><c:out value="${m[1]}"/></div></div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="ai-msg ai">
                                            <span class="ai-ava"><img src="${ctx}/static/images/logo.png" alt="小九"></span>
                                            <div class="ai-bubble"><c:out value="${m[1]}"/></div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="ai-suggest" id="aiSuggest">
                    <span class="ai-suggest-label">试试这些</span>
                    <button type="button" class="chip" data-q="帮我写一段自我介绍">帮我写自我介绍</button>
                    <button type="button" class="chip" data-q="在九组论坛怎么发帖？">怎么发帖？</button>
                    <button type="button" class="chip" data-q="推荐几个学 Java Web 的免费资源">学 Java 的资源</button>
                    <button type="button" class="chip" data-q="用一句话解释什么是 HTTP">解释 HTTP</button>
                    <button type="button" class="chip" data-q="Servlet 的生命周期是怎样的？">Servlet 生命周期</button>
                    <button type="button" class="chip" data-q="帮我解释一下这段代码为什么会空指针">Java 空指针排查</button>
                    <button type="button" class="chip" data-q="写一段 MySQL 分页查询的 SQL">分页 SQL</button>
                    <button type="button" class="chip" data-q="给我讲个程序员相关的冷笑话">讲个冷笑话</button>
                </div>

                <%-- 页面内「帮写」小面板：复用已有 compose 接口 --%>
                <div class="ai-write" id="aiWrite">
                    <button type="button" class="btn ghost sm" id="aiWriteBtn">
                        <svg class="i"><use href="#i-spark"></use></svg>帮写草稿
                    </button>
                    <div class="ai-write-panel" id="aiWritePanel" style="display:none">
                        <div class="ai-write-kind">
                            <button type="button" class="chip on" data-kind="post">写帖子</button>
                            <button type="button" class="chip" data-kind="reply">写回复</button>
                        </div>
                        <textarea class="textarea" id="aiWritePrompt" rows="3"
                                  placeholder="描述你想写的内容，例如：想发个帖问问大家周末去哪玩，顺便组队"></textarea>
                        <div class="ai-write-foot">
                            <span class="muted" id="aiWriteHint"></span>
                            <span style="flex:1"></span>
                            <button type="button" class="btn ghost" id="aiWriteClose">收起</button>
                            <button type="button" class="btn" id="aiWriteGo">生成</button>
                        </div>
                        <div class="ai-write-out" id="aiWriteOut" style="display:none">
                            <div class="ai-write-out-head">
                                <b>草稿</b>
                                <span style="flex:1"></span>
                                <button type="button" class="btn ghost sm" id="aiWriteCopy">复制</button>
                            </div>
                            <div class="ai-write-out-body" id="aiWriteBody"></div>
                            <div class="muted" style="font-size:11.5px;margin-top:6px">
                                复制后可以到
                                <a href="${ctx}/post/add">发帖页</a> 或帖子详情页的回复框粘贴。
                            </div>
                        </div>
                    </div>
                </div>

                <div class="ai-input">
                    <textarea id="aiText" rows="1" maxlength="2000"
                              placeholder="给小九发消息…（Enter 发送，Shift+Enter 换行）"></textarea>
                    <span class="ai-count" id="aiCount">0/2000</span>
                    <button class="btn primary" id="aiSend" type="button">
                        <svg class="i"><use href="#i-wave"></use></svg>发送
                    </button>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    (function () {
        var box = document.getElementById('aiMsgs');
        var text = document.getElementById('aiText');
        var send = document.getElementById('aiSend');
        var clear = document.getElementById('aiClear');
        var count = document.getElementById('aiCount');
        var suggest = document.getElementById('aiSuggest');
        if (!box || !text || !send) { return; }

        // ---------------------------------------------------------- Markdown 渲染
        // 先转义 HTML 再格式化，保证 AI 返回内容不会被当作标签执行（XSS 安全）
        function esc(s) {
            return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        }
        function inline(t) {
            return t.replace(/`([^`]+)`/g, '<code>$1</code>')
                    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        }
        function mdToHtml(src) {
            var lines = String(src).split(/\r?\n/);
            var out = [], inCode = false, codeBuf = [], listType = null, para = [];
            function flushPara() {
                if (!para.length) { return; }
                var body = inline(para.map(esc).join('<br>'));
                out.push('<p>' + body + '</p>');
                para = [];
            }
            function flushList() {
                if (listType) { out.push('</' + listType + '>'); listType = null; }
            }
            for (var i = 0; i < lines.length; i++) {
                var line = lines[i];
                var fence = line.match(/^\s*```(.*)$/);
                if (fence) {
                    if (!inCode) {
                        flushPara(); flushList();
                        inCode = true; codeBuf = [];
                    } else {
                        out.push('<pre><code>' + esc(codeBuf.join('\n')) + '</code></pre>');
                        inCode = false; codeBuf = [];
                    }
                    continue;
                }
                if (inCode) { codeBuf.push(line); continue; }
                if (/^\s*$/.test(line)) { flushPara(); flushList(); continue; }
                // 标题行：用字符计数而非正则，避免 JSP 把井号加花括号当成 EL 表达式解析
                var raw = line.replace(/^\s+/, '');
                var hashes = 0;
                while (hashes < raw.length && hashes < 7 && raw.charAt(hashes) === '#') { hashes++; }
                if (hashes > 0 && hashes <= 6 && raw.charAt(hashes) === ' ') {
                    flushPara(); flushList();
                    out.push('<strong>' + esc(raw.substring(hashes).trim()) + '</strong>');
                    continue;
                }
                var li = line.match(/^\s*[-*+]\s+(.*)$/);
                if (li) {
                    flushPara();
                    if (listType !== 'ul') { flushList(); listType = 'ul'; out.push('<ul>'); }
                    out.push('<li>' + inline(esc(li[1])) + '</li>');
                    continue;
                }
                var ol = line.match(/^\s*\d+[.)]\s+(.*)$/);
                if (ol) {
                    flushPara();
                    if (listType !== 'ol') { flushList(); listType = 'ol'; out.push('<ol>'); }
                    out.push('<li>' + inline(esc(ol[1])) + '</li>');
                    continue;
                }
                flushList();
                para.push(line);
            }
            flushPara(); flushList();
            if (inCode && codeBuf.length) {
                out.push('<pre><code>' + esc(codeBuf.join('\n')) + '</code></pre>');
            }
            return out.join('');
        }

        // ---------------------------------------------------------- 消息渲染
        function scroll() { box.scrollTop = box.scrollHeight; }

        function copyText(str, btn) {
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(str).then(function () {
                    btn.textContent = '已复制';
                    setTimeout(function () { btn.textContent = '复制'; }, 1400);
                });
            } else {
                var ta = document.createElement('textarea');
                ta.value = str; document.body.appendChild(ta); ta.select();
                try { document.execCommand('copy'); btn.textContent = '已复制'; } catch (e) {}
                document.body.removeChild(ta);
                setTimeout(function () { btn.textContent = '复制'; }, 1400);
            }
        }

        /** 给某条消息挂上操作按钮（复制 / 换个说法） */
        function addActs(msgNode, role, raw) {
            var bub = msgNode.querySelector('.ai-bubble');
            if (!bub) { return; }
            bub.setAttribute('data-raw', raw);
            var acts = document.createElement('div');
            acts.className = 'ai-acts';
            var cp = document.createElement('button');
            cp.type = 'button'; cp.className = 'ai-act'; cp.textContent = '复制';
            cp.onclick = function () { copyText(bub.getAttribute('data-raw') || '', cp); };
            acts.appendChild(cp);
            if (role === 'ai') {
                var again = document.createElement('button');
                again.type = 'button'; again.className = 'ai-act'; again.textContent = '换个说法';
                again.onclick = function () {
                    var prev = prevUserText(msgNode);
                    if (prev) { doSend(prev); }
                };
                acts.appendChild(again);
            }
            bub.appendChild(acts);
        }

        /** 向上找到最近一条自己发的原文 */
        function prevUserText(node) {
            var p = node.previousElementSibling;
            while (p) {
                if (p.classList && p.classList.contains('me')) {
                    var b = p.querySelector('.ai-bubble');
                    if (b) { return b.getAttribute('data-raw') || ''; }
                }
                p = p.previousElementSibling;
            }
            return '';
        }

        function addMsg(role, content) {
            var div = document.createElement('div');
            div.className = 'ai-msg ' + (role === 'me' ? 'me' : 'ai');
            if (role === 'me') {
                div.innerHTML = '<div class="ai-bubble"></div>';
                div.querySelector('.ai-bubble').textContent = content;
            } else {
                div.innerHTML = '<span class="ai-ava"><img src="${ctx}/static/images/logo.png" alt="小九"></span>'
                    + '<div class="ai-bubble"></div>';
                div.querySelector('.ai-bubble').innerHTML = mdToHtml(content);
            }
            box.appendChild(div);
            addActs(div, role, content);
            scroll();
            return div;
        }

        function typing() {
            var div = document.createElement('div');
            div.className = 'ai-msg ai typing';
            div.innerHTML = '<span class="ai-ava"><img src="${ctx}/static/images/logo.png" alt="小九"></span>'
                + '<div class="ai-bubble"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div>';
            box.appendChild(div);
            scroll();
            return div;
        }

        function hideSuggest() { if (suggest) { suggest.style.display = 'none'; } }

        // 把服务端渲染出来的历史消息也渲染成 Markdown（并在最后一条挂上按钮）
        (function renderExisting() {
            var nodes = box.querySelectorAll('.ai-msg');
            for (var i = 0; i < nodes.length; i++) {
                var bub = nodes[i].querySelector('.ai-bubble');
                if (!bub || bub.getAttribute('data-md')) { continue; }
                var role = nodes[i].classList.contains('me') ? 'me' : 'ai';
                var raw = bub.textContent;
                if (role === 'ai') { bub.innerHTML = mdToHtml(raw); }
                bub.setAttribute('data-md', '1');
                addActs(nodes[i], role, raw);
            }
        })();

        var busy = false;
        function doSend(msgOverride) {
            var msg = (msgOverride != null) ? msgOverride : text.value.trim();
            if (!msg || busy) { return; }
            busy = true;
            send.disabled = true;
            text.value = '';
            if (count) { count.textContent = '0/2000'; }
            autoGrow();
            hideSuggest();
            addMsg('me', msg);
            var tip = typing();
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '${ctx}/ai/chat', true);
            xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
            xhr.onreadystatechange = function () {
                if (xhr.readyState !== 4) { return; }
                if (tip.parentNode) { tip.parentNode.removeChild(tip); }
                busy = false;
                send.disabled = false;
                try {
                    var data = JSON.parse(xhr.responseText);
                    if (data.ok) {
                        addMsg('ai', data.reply);
                    } else {
                        addMsg('ai', '（出错了：' + (data.error || '未知错误') + '）');
                    }
                } catch (e) {
                    addMsg('ai', '（网络异常，请稍后再试）');
                }
                text.focus();
            };
            xhr.send(JSON.stringify({ message: msg }));
        }

        function autoGrow() {
            text.style.height = 'auto';
            text.style.height = Math.min(text.scrollHeight, 140) + 'px';
        }

        send.onclick = function () { doSend(); };
        text.oninput = function () {
            if (count) { count.textContent = text.value.length + '/2000'; }
            autoGrow();
        };
        text.onkeydown = function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                doSend();
            }
        };

        if (suggest) {
            var chips = suggest.querySelectorAll('.chip');
            for (var i = 0; i < chips.length; i++) {
                chips[i].onclick = function () { doSend(this.getAttribute('data-q')); };
            }
        }

        if (clear) {
            clear.onclick = function () {
                if (busy) { return; }
                var xhr = new XMLHttpRequest();
                xhr.open('POST', '${ctx}/ai/chat', true);
                xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
                xhr.onload = function () { location.reload(); };
                xhr.send(JSON.stringify({ action: 'clear' }));
            };
        }

        // ---------------------------------------------------------- 页面内「帮写」
        (function initWrite() {
            var btn = document.getElementById('aiWriteBtn');
            var panel = document.getElementById('aiWritePanel');
            var closeBtn = document.getElementById('aiWriteClose');
            var go = document.getElementById('aiWriteGo');
            var promptEl = document.getElementById('aiWritePrompt');
            var hint = document.getElementById('aiWriteHint');
            var out = document.getElementById('aiWriteOut');
            var body = document.getElementById('aiWriteBody');
            var copyBtn = document.getElementById('aiWriteCopy');
            var kindBtns = panel ? panel.querySelectorAll('.ai-write-kind .chip') : [];
            if (!btn || !panel) { return; }
            var kind = 'post';

            btn.onclick = function () {
                panel.style.display = panel.style.display === 'none' ? 'block' : 'none';
            };
            closeBtn.onclick = function () { panel.style.display = 'none'; };
            for (var k = 0; k < kindBtns.length; k++) {
                kindBtns[k].onclick = function () {
                    for (var j = 0; j < kindBtns.length; j++) { kindBtns[j].className = 'chip'; }
                    this.className = 'chip on';
                    kind = this.getAttribute('data-kind');
                    if (kind === 'reply') {
                        promptEl.placeholder = '说说你想回什么，例如：同意楼主，补充一下周末可能下雨，记得带伞';
                    } else {
                        promptEl.placeholder = '描述你想写的帖子，例如：想发个帖问问大家周末去哪玩，顺便组队';
                    }
                };
            }

            go.onclick = function () {
                var p = promptEl.value.trim();
                if (!p) { hint.textContent = '先描述一下你想写什么'; return; }
                go.disabled = true; go.textContent = '生成中…';
                hint.textContent = 'AI 正在起稿…';
                out.style.display = 'none';
                fetch('${ctx}/ai/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ action: 'compose', kind: kind, prompt: p })
                }).then(function (r) { return r.json(); }).then(function (d) {
                    if (d.ok) {
                        var draft = (d.reply || '').trim();
                        body.innerHTML = mdToHtml(draft);
                        body.setAttribute('data-raw', draft);
                        out.style.display = 'block';
                        hint.textContent = '已生成，可复制后粘贴使用';
                        copyBtn.textContent = '复制';
                    } else {
                        hint.textContent = d.error || '生成失败';
                    }
                }).catch(function () {
                    hint.textContent = '网络错误，请重试';
                }).then(function () {
                    go.disabled = false; go.textContent = '生成';
                });
            };

            copyBtn.onclick = function () {
                copyText(body.getAttribute('data-raw') || body.textContent || '', copyBtn);
            };
        })();

        scroll();
        text.focus();
    })();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
