package com.jiuzu.forum.util;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP 工具：获取客户端真实 IP 并解析属地
 *
 * <p>现代论坛（贴吧 / 微博 / 掘金）普遍展示「IP 属地」，只显示到省一级，
 * 不暴露完整地址。本工具遵循同样口径：</p>
 * <ul>
 *   <li>{@link #getClientIp} 穿透反向代理与 CDN，拿到客户端真实 IP</li>
 *   <li>{@link #getLocation} 返回属地文案；公网 IP 走在线解析并缓存，
 *       解析失败自动降级为掩码（如 113.66.*.*），绝不阻塞发帖</li>
 *   <li>{@link #mask} 把 IP 打码，前台展示用</li>
 * </ul>
 *
 * <p>完整 IP 仍然入库，但只供后台管理查看，前台一律不展示。</p>
 *
 * @author 第九小组
 */
public class IpUtil {

    /** 本机 / 内网地址的属地文案 */
    public static final String LOC_LOCAL = "本机";
    public static final String LOC_LAN = "局域网";
    public static final String LOC_UNKNOWN = "未知";

    /** 属地缓存：ip -> 属地文案 */
    private static final Map<String, String> CACHE = new ConcurrentHashMap<String, String>();

    /** 在线解析连续失败次数，超过阈值后暂时熔断，避免每次发帖都卡住 */
    private static volatile int failCount = 0;
    private static volatile long disableUntil = 0L;
    private static final int FAIL_LIMIT = 3;
    private static final long DISABLE_MS = 10 * 60 * 1000L;

    /** 在线解析超时（毫秒） */
    private static final int TIMEOUT = 1500;

    private static final Pattern ADDR = Pattern.compile("\"addr\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern PRO = Pattern.compile("\"pro\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern CITY = Pattern.compile("\"city\"\\s*:\\s*\"([^\"]*)\"");

    private IpUtil() {
    }

    /**
     * 获取客户端真实 IP（自动穿透 Nginx / CDN 反向代理）
     *
     * @param request 当前请求
     * @return IP 字符串，取不到时返回空串
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = header(request, "X-Forwarded-For");
        if (isEmpty(ip)) {
            ip = header(request, "X-Real-IP");
        }
        if (isEmpty(ip)) {
            ip = header(request, "Proxy-Client-IP");
        }
        if (isEmpty(ip)) {
            ip = header(request, "WL-Proxy-Client-IP");
        }
        if (isEmpty(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null) {
            return "";
        }
        // 多层代理时取第一个有效 IP
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // 本地测试的 IPv6 回环地址转成 IPv4，方便展示
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip.trim();
    }

    /** 读取请求头，过滤掉 unknown 占位值 */
    private static String header(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        if (v == null || v.trim().isEmpty() || "unknown".equalsIgnoreCase(v.trim())) {
            return "";
        }
        return v.trim();
    }

    /** 是否为内网 / 本机地址 */
    public static boolean isPrivate(String ip) {
        if (isEmpty(ip)) {
            return true;
        }
        if ("127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return true;
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            String[] p = ip.split("\\.");
            if (p.length == 4) {
                try {
                    int second = Integer.parseInt(p[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        if (ip.startsWith("169.254.")) {
            return true;
        }
        return false;
    }

    /**
     * 解析 IP 属地
     *
     * <p>优先级：缓存 → 内网判断 → 在线解析（带熔断）→ 掩码兜底。</p>
     *
     * @param ip IP 地址，可为空
     * @return 属地文案，如「广东」「本机」「113.66.*.*」
     */
    public static String getLocation(String ip) {
        if (isEmpty(ip)) {
            return LOC_UNKNOWN;
        }
        String cached = CACHE.get(ip);
        if (cached != null) {
            return cached;
        }
        if (isPrivate(ip)) {
            String loc = "127.0.0.1".equals(ip) ? LOC_LOCAL : LOC_LAN;
            CACHE.put(ip, loc);
            return loc;
        }

        String loc = queryOnline(ip);
        if (isEmpty(loc)) {
            loc = mask(ip);
        }
        CACHE.put(ip, loc);
        return loc;
    }

    /**
     * 把 IP 打码：保留前两段，其余用 * 代替
     *
     * @param ip IP 地址
     * @return 如 113.66.*.*
     */
    public static String mask(String ip) {
        if (isEmpty(ip) || isPrivate(ip)) {
            return isEmpty(ip) ? LOC_UNKNOWN : ("127.0.0.1".equals(ip) ? LOC_LOCAL : LOC_LAN);
        }
        String[] p = ip.split("\\.");
        if (p.length == 4) {
            return p[0] + "." + p[1] + ".*.*";
        }
        // IPv6 等非四段格式：只保留前两段
        String[] c = ip.split(":");
        if (c.length >= 2) {
            return c[0] + ":" + c[1] + ":****";
        }
        return ip;
    }

    /**
     * 在线查询归属地（太平洋网络 whois 接口，国内可达）
     *
     * <p>任何异常都返回 null，由调用方降级，不会影响主流程。</p>
     *
     * @param ip 公网 IP
     * @return 属地文案（如「广东省广州市 电信」），失败返回 null
     */
    private static String queryOnline(String ip) {
        if (System.currentTimeMillis() < disableUntil) {
            return null; // 熔断期内不再请求
        }
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 jiuzu-forum");
            int code = conn.getResponseCode();
            if (code != 200) {
                fail();
                return null;
            }
            byte[] data = readAll(conn.getInputStream());
            // 该接口返回 GBK，先按 GBK 解，出现乱码再按 UTF-8 重试
            String body = new String(data, "GBK");
            if (body.indexOf('\ufffd') >= 0) {
                body = new String(data, "UTF-8");
            }
            String loc = firstGroup(ADDR, body);
            if (isEmpty(loc)) {
                String pro = firstGroup(PRO, body);
                String city = firstGroup(CITY, body);
                loc = (isEmpty(pro) ? "" : pro) + (isEmpty(city) ? "" : city);
            }
            if (isEmpty(loc)) {
                fail();
                return null;
            }
            failCount = 0;
            return loc.trim();
        } catch (Exception e) {
            fail();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /** 正则取第一个分组 */
    private static String firstGroup(Pattern p, String body) {
        Matcher m = p.matcher(body);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    /** 记录一次失败，连续失败达到阈值就熔断一段时间 */
    private static void fail() {
        failCount++;
        if (failCount >= FAIL_LIMIT) {
            disableUntil = System.currentTimeMillis() + DISABLE_MS;
            failCount = 0;
        }
    }

    /** 读取流全部字节 */
    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
        return bos.toByteArray();
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
