package com.jiuzu.forum.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正文排版工具：把用户输入的轻量 Markdown 渲染成安全的 HTML
 *
 * <p>支持的语法（够用即可，不做完整 Markdown 解析）：</p>
 * <ul>
 *   <li>图片：![说明](/uploads/content/xxx.png)</li>
 *   <li>加粗：**文字**</li>
 *   <li>行内代码：`代码`</li>
 *   <li>代码块：``` 包裹</li>
 *   <li>引用：以 &gt; 开头的行</li>
 *   <li>分割线：单独一行 ---</li>
 *   <li>普通换行转 &lt;br&gt;</li>
 * </ul>
 *
 * <p>安全策略：先整体 HTML 转义，再只放行上述语法生成的标签，
 * 因此用户手写 &lt;script&gt; 也不会被执行；图片地址只接受站内 /uploads/ 与 http(s)。</p>
 *
 * @author 第九小组
 */
public final class MarkupUtil {

    /** 图片语法 ![alt](url) */
    private static final Pattern IMG = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)\\s]+)\\)");
    /** 加粗 **文字** */
    private static final Pattern BOLD = Pattern.compile("\\*\\*([^*\\n]+)\\*\\*");
    /** 行内代码 `代码` */
    private static final Pattern CODE = Pattern.compile("`([^`\\n]+)`");

    private MarkupUtil() {
    }

    /**
     * 渲染正文为 HTML（已转义，可直接在页面输出）
     *
     * @param src 原始文本
     * @return 安全的 HTML 片段
     */
    public static String render(String src) {
        if (src == null || src.trim().isEmpty()) {
            return "";
        }
        String text = src.replace("\r\n", "\n").replace("\r", "\n");

        // 先抽出代码块，避免块内内容被后续规则误伤
        StringBuilder out = new StringBuilder();
        String[] parts = text.split("```", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1) {
                // 代码块内部
                out.append("<pre class=\"md-pre\"><code>")
                        .append(escapeHtml(trimNewline(parts[i])))
                        .append("</code></pre>");
            } else {
                out.append(inline(parts[i]));
            }
        }
        return out.toString();
    }

    /** 代码块之外的行内排版 */
    private static String inline(String block) {
        StringBuilder sb = new StringBuilder();
        String[] lines = block.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String t = escapeHtml(line);

            // 分割线
            if (t.trim().equals("---")) {
                sb.append("<hr class=\"md-hr\">");
            } else if (t.trim().startsWith("&gt;")) {
                // 引用
                sb.append("<blockquote class=\"md-quote\">")
                        .append(t.trim().replaceFirst("&gt;\\s*", ""))
                        .append("</blockquote>");
            } else {
                // 图片、行内代码、加粗
                t = replaceImage(t);
                t = CODE.matcher(t).replaceAll("<code class=\"md-code\">$1</code>");
                t = BOLD.matcher(t).replaceAll("<strong>$1</strong>");
                sb.append(t);
            }
            if (i < lines.length - 1) {
                sb.append("<br>");
            }
        }
        return sb.toString();
    }

    /** 把 ![alt](url) 换成 img，并校验地址白名单 */
    private static String replaceImage(String escapedLine) {
        Matcher m = IMG.matcher(escapedLine);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String alt = m.group(1);
            String url = m.group(2);
            String safe = isSafeUrl(url) ? url : "";
            String rep = safe.isEmpty()
                    ? ""
                    : "<img class=\"md-img\" src=\"" + safe + "\" alt=\"" + alt + "\" data-zoom loading=\"lazy\">";
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** 只放行站内上传目录与 http(s) 地址 */
    private static boolean isSafeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String lower = url.toLowerCase();
        // 站内地址可能是 /uploads/...，也可能是带上下文的 /jiuzu-forum/uploads/...
        if (lower.startsWith("/")) {
            return lower.contains("/uploads/");
        }
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /** 去掉 Markdown 标记，用于列表页摘要 */
    public static String plain(String src) {
        if (src == null) {
            return "";
        }
        String t = src.replace("\r\n", "\n");
        t = IMG.matcher(t).replaceAll("[图片]");
        t = t.replace("```", " ").replace("**", "").replace("`", "");
        t = t.replaceAll("(?m)^>\\s*", "");
        t = t.replaceAll("(?m)^-{3,}$", " ");
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }

    private static String trimNewline(String s) {
        return s.replaceAll("^\\n+", "").replaceAll("\\n+$", "");
    }

    /** HTML 转义 */
    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 32);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
