package com.jiuzu.forum.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局编码过滤器
 *
 * <p>统一设置请求与响应的字符集为 UTF-8，解决中文乱码问题，
 * JavaScript/AJAX 场景下也一并生效。</p>
 *
 * @author 第九小组
 */
public class EncodingFilter implements Filter {

    /** 是否由当前容器负责响应编码 */
    private boolean forceResponse = true;

    public void init(FilterConfig filterConfig) throws ServletException {
        String force = filterConfig.getInitParameter("forceResponseEncoding");
        if (force != null) {
            forceResponse = Boolean.parseBoolean(force);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (forceResponse) {
            resp.setHeader("Content-Type", "text/html;charset=UTF-8");
        }
        // 记录基础路径，JSP 中可用 ${ctx} 拼接资源与链接
        req.setAttribute("ctx", req.getContextPath());
        chain.doFilter(req, resp);
    }

    public void destroy() {
        // 无需释放资源
    }
}
