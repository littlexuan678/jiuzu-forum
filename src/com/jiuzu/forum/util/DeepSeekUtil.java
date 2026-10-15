package com.jiuzu.forum.util;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * DeepSeek 大模型调用工具类
 *
 * <p>用 JDK 原生 HttpURLConnection 直连 DeepSeek 官方 API
 * （POST /chat/completions），不引入任何第三方 JSON 库，
 * 请求体与响应均手工拼装/解析，符合课程"轻量、可控"的要求。</p>
 *
 * <p>配置统一放在 /WEB-INF/config.properties（不进版本库）：</p>
 * <pre>
 * deepseek.api.key=sk-xxxx
 * deepseek.base.url=https://api.deepseek.com/v1
 * deepseek.model=deepseek-chat
 * </pre>
 *
 * @author 第九小组
 */
public class DeepSeekUtil {

    private static volatile boolean inited = false;
    private static String apiKey = "";
    private static String baseUrl = "https://api.deepseek.com/v1";
    private static String model = "deepseek-chat";

    /** 应用启动时由 AiServlet 调用；若尚未就绪（配置文件缺失/未加载）会重新尝试读取，
     *  这样即便配置文件在应用启动后补传，下一次请求也能自动生效，避免永久“不可用”。 */
    public static synchronized void init(ServletContext ctx) {
        if (inited && isReady()) {
            return;
        }
        InputStream in = null;
        try {
            in = ctx.getResourceAsStream("/WEB-INF/config.properties");
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                apiKey = trim(p.getProperty("deepseek.api.key"));
                baseUrl = trim(p.getProperty("deepseek.base.url"));
                if (baseUrl.isEmpty()) {
                    baseUrl = "https://api.deepseek.com/v1";
                }
                model = trim(p.getProperty("deepseek.model"));
                if (model.isEmpty()) {
                    model = "deepseek-chat";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(in);
        }
        inited = true;
    }

    /** 是否已完成配置且提供了 API Key */
    public static boolean isReady() {
        return inited && apiKey.length() > 10;
    }

    /**
     * 发起多轮对话
     *
     * @param messages 消息数组，每条为 {role, content}，role 取 "system"/"user"/"assistant"
     * @return 模型回复文本
     */
    public static String chat(String[][] messages) throws IOException {
        String body = buildRequestBody(messages, model);
        HttpResponse rsp = postJson(baseUrl + "/chat/completions", body);

        // 模型名不对时（老账号别名差异）自动降级重试一次
        if (rsp.code == 400 && rsp.body.contains("Model Not Exist") && !"deepseek-chat".equals(model)) {
            body = buildRequestBody(messages, "deepseek-chat");
            rsp = postJson(baseUrl + "/chat/completions", body);
        }
        if (rsp.code != 200) {
            String reason = extractError(rsp.body);
            throw new IOException("DeepSeek 接口返回 " + rsp.code + "：" + reason);
        }
        String content = extractContent(rsp.body);
        if (content == null || content.isEmpty()) {
            throw new IOException("DeepSeek 返回内容为空");
        }
        return content;
    }

    // ---------------------------------------------------------------- 内部实现

    /** 拼装 chat/completions 请求体（手工构造 JSON） */
    private static String buildRequestBody(String[][] messages, String useModel) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"model\":\"").append(escapeJson(useModel)).append('"');
        sb.append(",\"stream\":false,\"temperature\":0.7,\"max_tokens\":2048");
        sb.append(",\"messages\":[");
        for (int i = 0; i < messages.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"role\":\"").append(escapeJson(messages[i][0]))
              .append("\",\"content\":\"").append(escapeJson(messages[i][1])).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /** 发送 JSON POST 并读取响应 */
    private static HttpResponse postJson(String urlStr, String body) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(90000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            StringBuilder out = new StringBuilder();
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line);
                }
                close(is);
            }
            return new HttpResponse(code, out.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 从响应 JSON 中提取 choices[0].message.content。
     * 手工解析：定位 "message" 对象内的 "content" 字符串，并处理转义。
     */
    private static String extractContent(String json) {
        if (json == null) {
            return null;
        }
        int m = json.indexOf("\"message\"");
        int c = m >= 0 ? json.indexOf("\"content\"", m) : json.indexOf("\"content\"");
        if (c < 0) {
            return null;
        }
        int q1 = json.indexOf('"', c + 9);
        if (q1 < 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = q1 + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '"') {
                return sb.toString();
            }
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

    /** 从错误响应中尽量提取 message 字段，便于前端提示 */
    private static String extractError(String json) {
        String s = extractContent(json);
        if (s != null && !s.isEmpty()) {
            return s;
        }
        return json == null ? "未知错误" : (json.length() > 200 ? json.substring(0, 200) : json);
    }

    /** 最小 JSON 字符串转义 */
    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
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

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static void close(InputStream in) {
        if (in != null) {
            try { in.close(); } catch (IOException ignore) {}
        }
    }

    /** 简单的响应封装 */
    private static final class HttpResponse {
        final int code;
        final String body;
        HttpResponse(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }
}
