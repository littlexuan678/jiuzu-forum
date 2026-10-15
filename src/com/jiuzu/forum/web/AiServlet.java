package com.jiuzu.forum.web;

import com.jiuzu.forum.util.DeepSeekUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 助手控制器（接入 DeepSeek 大模型）
 *
 * <p>GET  /ai      → AI 聊天页面</p>
 * <p>POST /ai/chat → 发送一条消息，返回模型回复（JSON）</p>
 *
 * <p>多轮上下文保存在 Session 中（最多保留最近 12 条），
 * 前端传 JSON：{"message":"..."} 或 {"action":"clear"}。</p>
 *
 * @author 第九小组
 */
public class AiServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    /** Session 中保存对话历史的 key */
    private static final String HISTORY_KEY = "aiHistory";

    /** 携带给模型的人设提示词 */
    private static final String SYSTEM_PROMPT =
            "你是「九组论坛」的 AI 助手小九，一个热情、幽默、简洁的中文论坛助手。"
            + "回答保持口语化、友好，默认不超过 300 字；"
            + "遇到与论坛相关的问题（如何发帖、注册、版规等）按常理引导即可。";

    /** 上下文最多保留的消息条数（不含 system） */
    private static final int MAX_HISTORY = 12;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        DeepSeekUtil.init(getServletContext());
        req.setAttribute("pageTitle", "AI 助手");
        req.setAttribute("aiReady", DeepSeekUtil.isReady());
        List<String[]> history = getHistory(req.getSession());
        req.setAttribute("history", history);
        forward(req, resp, "ai/chat.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        DeepSeekUtil.init(getServletContext());
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String body = readBody(req);

        // AI 帮写草稿（发帖 / 回复）：action=compose
        String action = extractField(body, "action");
        if ("compose".equals(action)) {
            handleCompose(req, resp, out, body);
            return;
        }

        // 清空对话
        if (body.contains("\"clear\"")) {
            req.getSession().removeAttribute(HISTORY_KEY);
            writeJson(out, true, "ok", "");
            return;
        }

        String message = extractField(body, "message");
        if (message.isEmpty()) {
            writeJson(out, false, "消息不能为空", "");
            return;
        }
        if (!DeepSeekUtil.isReady()) {
            writeJson(out, false, "AI 助手未配置 API Key，请联系管理员在 WEB-INF/config.properties 中配置", "");
            return;
        }
        if (message.length() > 2000) {
            message = message.substring(0, 2000);
        }

        HttpSession session = req.getSession();
        List<String[]> history = getHistory(session);
        history.add(new String[]{"user", message});

        try {
            String[][] messages = buildMessages(history);
            String reply = DeepSeekUtil.chat(messages);
            history.add(new String[]{"assistant", reply});
            // 只保留最近 MAX_HISTORY 条
            while (history.size() > MAX_HISTORY) {
                history.remove(0);
            }
            session.setAttribute(HISTORY_KEY, history);
            writeJson(out, true, "", reply);
        } catch (Exception e) {
            writeJson(out, false, "AI 出错了：" + e.getMessage(), "");
        }
    }

    /**
     * 处理「AI 帮写草稿」请求：根据 kind（post/reply）构造不同的提示词，
     * 调用 DeepSeek 生成一篇草稿，原样返回给前端自行解析填表。
     */
    private void handleCompose(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, String body)
            throws IOException {
        DeepSeekUtil.init(getServletContext());
        if (!DeepSeekUtil.isReady()) {
            writeJson(out, false, "AI 助手未配置 API Key，请在 WEB-INF/config.properties 中配置", "");
            return;
        }
        String kind = extractField(body, "kind");
        String prompt = extractField(body, "prompt");
        if (prompt.isEmpty()) {
            writeJson(out, false, "请先描述你想写什么", "");
            return;
        }
        if (prompt.length() > 2000) {
            prompt = prompt.substring(0, 2000);
        }

        String sys;
        String userMsg;
        if ("reply".equals(kind)) {
            sys = "你是「九组论坛」的回复助手。根据用户提供的帖子上下文和想法，写一段适合论坛回复的中文内容，"
                    + "语气友善、口语化，80-200 字，不要使用 Markdown 标题或代码块，只输出回复正文。";
            userMsg = "请帮我写一条回复，我的想法/上下文如下：\n" + prompt;
        } else {
            sys = "你是「九组论坛」的发帖助手。根据用户给的主题或要点，生成一篇可直接发布的论坛帖子草稿。"
                    + "严格按如下格式输出：第一行写【标题】后面跟一句话标题；第二行起写正文，结构清晰、语气自然，200-400 字。"
                    + "不要使用 Markdown 代码块或额外说明。";
            userMsg = "我要发一个帖子，主题/要点如下：\n" + prompt + "\n请按格式输出标题和正文。";
        }

        try {
            String reply = DeepSeekUtil.chat(new String[][]{{"system", sys}, {"user", userMsg}});
            writeJson(out, true, "", reply);
        } catch (Exception e) {
            writeJson(out, false, "AI 出错了：" + e.getMessage(), "");
        }
    }

    // ---------------------------------------------------------------- 内部实现

    /** 组装发给 DeepSeek 的消息序列：system + 历史 */
    private String[][] buildMessages(List<String[]> history) {
        String[][] messages = new String[history.size() + 1][2];
        messages[0][0] = "system";
        messages[0][1] = SYSTEM_PROMPT;
        for (int i = 0; i < history.size(); i++) {
            messages[i + 1][0] = history.get(i)[0];
            messages[i + 1][1] = history.get(i)[1];
        }
        return messages;
    }

    @SuppressWarnings("unchecked")
    private List<String[]> getHistory(HttpSession session) {
        Object o = session.getAttribute(HISTORY_KEY);
        if (o instanceof List) {
            return (List<String[]>) o;
        }
        return new ArrayList<String[]>();
    }

    /** 读取 POST 原始 JSON 体 */
    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = req.getReader();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (br != null) { br.close(); }
        }
        return sb.toString();
    }

    /** 从请求 JSON 中提取某个字符串字段（含转义还原），body 为 {"message":"..."} */
    private String extractField(String json, String field) {
        if (json == null) { return ""; }
        int k = json.indexOf("\"" + field + "\"");
        if (k < 0) { return ""; }
        int colon = json.indexOf(':', k + field.length() + 2);
        if (colon < 0) { return ""; }
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) { return ""; }
        StringBuilder sb = new StringBuilder();
        for (int i = q1 + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '"') { break; }
            if (ch == '\\' && i + 1 < json.length()) {
                char n = json.charAt(++i);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': break;
                    case 'u':
                        if (i + 4 < json.length()) {
                            sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                        break;
                    default: sb.append(n);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** 输出统一格式 JSON：{"ok":bool,"error":"...","reply":"..."} */
    private void writeJson(PrintWriter out, boolean ok, String error, String reply) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":").append(ok);
        sb.append(",\"error\":\"").append(escape(error)).append('"');
        sb.append(",\"reply\":\"").append(escape(reply)).append("\"}");
        out.write(sb.toString());
        out.flush();
    }

    private String escape(String s) {
        if (s == null) { return ""; }
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}
