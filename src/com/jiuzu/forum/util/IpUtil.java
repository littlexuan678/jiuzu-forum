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
 * 不暴露任何 IP 信息。本工具遵循同样口径：</p>
 * <ul>
 *   <li>{@link #getClientIp} 穿透反向代理与 CDN，拿到客户端真实 IP（完整 IP 只入库供后台查看）</li>
 *   <li>{@link #getLocation} 返回省一级属地文案（如「广东」「内蒙古」「美国」）并缓存；
 *       解析失败返回空串，前台不展示属地徽章，绝不阻塞发帖</li>
 * </ul>
 *
 * <p>完整 IP 仍然入库，但只供后台管理查看，前台一律不展示（包括打码形式）。</p>
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

    /** 在线解析连接超时（毫秒）。云上首次 TLS 握手较慢，留足余量避免误判失败 */
    private static final int CONNECT_TIMEOUT = 3000;
    /** 在线解析读取超时（毫秒） */
    private static final int READ_TIMEOUT = 4000;

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
     * 解析 IP 属地（省一级）
     *
     * <p>优先级：缓存 → 内网判断 → 在线解析（带熔断）。解析失败返回空串，
     * 由前台隐藏属地徽章（不显示任何形式的 IP）。失败结果不缓存，
     * 网络恢复后下次发帖可自动重试。</p>
     *
     * @param ip IP 地址，可为空
     * @return 属地文案（如「广东」「本机」「美国」），解析不出返回空串
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
            // 解析失败：返回空串（前台不展示徽章），不缓存以便网络恢复后重试
            return "";
        }
        CACHE.put(ip, loc);
        return loc;
    }

    /**
     * 把 IP 打码：保留前两段，其余用 * 代替
     *
     * <p>当前前台已不展示任何形式的 IP，此方法仅作工具保留。</p>
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
     * 在线查询归属地并归一化为省一级（太平洋网络 whois 接口，国内可达）
     *
     * <p>任何异常都返回 null，由调用方降级，不会影响主流程。</p>
     *
     * @param ip 公网 IP
     * @return 省一级属地文案（如「广东」「内蒙古」「美国」），失败返回 null
     */
    private static String queryOnline(String ip) {
        if (System.currentTimeMillis() < disableUntil) {
            return null; // 熔断期内不再请求
        }
        HttpURLConnection conn = null;
        try {
            // 注意：该接口 HTTP 已返回 403（openresty 拦截），必须用 HTTPS
            URL url = new URL("https://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 jiuzu-forum");
            // 部分镜像会校验 Referer，补上同源域名规避 403
            conn.setRequestProperty("Referer", "https://whois.pconline.com.cn/");
            conn.setRequestProperty("Accept", "*/*");
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
            String loc = toRegion(firstGroup(PRO, body), firstGroup(CITY, body), firstGroup(ADDR, body));
            if (isEmpty(loc)) {
                fail();
                return null;
            }
            failCount = 0;
            return loc;
        } catch (Exception e) {
            fail();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 把接口返回的地址归一化为省一级属地文案
     *
     * <p>优先取省份字段（「广东省」→「广东」，「广西壮族自治区」→「广西」，
     * 直辖市「北京市」→「北京」）；省份缺失时从完整地址截省；
     * 省市都缺失时多为境外，直接用国家/地区名（如「美国」）。</p>
     *
     * @param pro  省份，如「广东省」，可能为空
     * @param city 城市，如「广州市」，可能为空
     * @param addr 完整地址，如「广东省广州市 电信」或「美国」，可能为空
     * @return 省一级属地文案，解析不出返回空串
     */
    private static String toRegion(String pro, String city, String addr) {
        String p = safeTrim(pro);
        if (!isEmpty(p)) {
            return stripSuffix(p);
        }
        String a = safeTrim(addr);
        if (!isEmpty(a)) {
            String head = a.split("\\s+")[0]; // 去掉运营商等尾缀
            if (head.contains("保留") || head.contains("局域网")) {
                return ""; // 保留地址等无意义归属
            }
            int provinceIdx = head.indexOf('省');
            if (provinceIdx > 0) {
                return stripSuffix(head.substring(0, provinceIdx));
            }
            int cityIdx = head.indexOf('市');
            if (cityIdx > 0) {
                return stripSuffix(head.substring(0, cityIdx));
            }
            // 省市都没有：多为境外国家/地区（如「美国」），直接使用
            if (head.length() <= 12 && !head.contains("*")) {
                return head;
            }
        }
        String c = safeTrim(city);
        return isEmpty(c) ? "" : stripSuffix(c);
    }

    /** 去掉行政区划后缀：维吾尔/回族/壮族/自治区、省、市 */
    private static String stripSuffix(String s) {
        String v = safeTrim(s);
        String[] suffixes = {"维吾尔自治区", "回族自治区", "壮族自治区", "自治区", "省", "市"};
        for (String suf : suffixes) {
            if (v.length() > suf.length() && v.endsWith(suf)) {
                return v.substring(0, v.length() - suf.length());
            }
        }
        return v;
    }

    /** null 安全去空格 */
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
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
