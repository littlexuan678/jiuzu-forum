<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- AI 助手：接入 DeepSeek 大模型的站内聊天页 --%>
<div class="container ai-page">
    <div class="ai-head card">
        <div class="ai-logo"><svg class="i"><use href="#i-zap"></use></svg></div>
        <div>
            <h1>AI 助手 <b>小九</b></h1>
            <p>由 DeepSeek 大模型驱动，问答、闲聊、写文案都可以，上下文连续多轮。</p>
        </div>
        <button class="btn ghost sm" id="aiClear" type="button">
            <svg class="i"><use href="#i-trash"></use></svg>清空对话
        </button>
    </div>

    <c:choose>
        <c:when test="${not aiReady}">
            <div class="card empty" style="padding:40px;text-align:center">
                <b>AI 助手尚未配置</b>
                <p class="muted" style="margin-top:8px">请在服务器 WEB-INF/config.properties 中填入 DeepSeek API Key。</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card ai-box">
                <div class="ai-msgs" id="aiMsgs">
                    <c:choose>
                        <c:when test="${empty history}">
                            <div class="ai-msg ai">
                                <span class="ai-ava"><svg class="i"><use href="#i-zap"></use></svg></span>
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
                                            <span class="ai-ava"><svg class="i"><use href="#i-zap"></use></svg></span>
                                            <div class="ai-bubble"><c:out value="${m[1]}"/></div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="ai-input">
                    <textarea id="aiText" rows="2" maxlength="2000"
                              placeholder="给小九发消息…（Enter 发送，Shift+Enter 换行）"></textarea>
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
        if (!box || !text || !send) { return; }

        function esc(s) {
            return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;')
                .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        }
        function scroll() { box.scrollTop = box.scrollHeight; }
        function addMsg(role, content) {
            var div = document.createElement('div');
            div.className = 'ai-msg ' + (role === 'me' ? 'me' : 'ai');
            if (role === 'me') {
                div.innerHTML = '<div class="ai-bubble">' + esc(content) + '</div>';
            } else {
                div.innerHTML = '<span class="ai-ava"><svg class="i"><use href="#i-zap"></use></svg></span>'
                    + '<div class="ai-bubble"></div>';
                div.querySelector('.ai-bubble').textContent = content;
            }
            box.appendChild(div);
            scroll();
            return div;
        }
        function typing() {
            var div = document.createElement('div');
            div.className = 'ai-msg ai typing';
            div.innerHTML = '<span class="ai-ava"><svg class="i"><use href="#i-zap"></use></svg></span>'
                + '<div class="ai-bubble"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div>';
            box.appendChild(div);
            scroll();
            return div;
        }

        var busy = false;
        function doSend() {
            var msg = text.value.trim();
            if (!msg || busy) { return; }
            busy = true;
            send.disabled = true;
            text.value = '';
            addMsg('me', msg);
            var tip = typing();
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '${ctx}/ai/chat', true);
            xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
            xhr.onreadystatechange = function () {
                if (xhr.readyState !== 4) { return; }
                tip.parentNode.removeChild(tip);
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

        send.onclick = doSend;
        text.onkeydown = function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                doSend();
            }
        };
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
        scroll();
        text.focus();
    })();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
