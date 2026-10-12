package com.jiuzu.forum.filter;

import com.jiuzu.forum.bean.User;

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
 * 登录状态过滤器
 *
 * <p>对需要登录后才能访问的 URL（发帖、回复、点赞、后台管理）做统一拦截，
 * 未登录时跳转到登录页，并记录原始请求地址以便登录后自动跳回。</p>
 *
 * @author 第九小组
 */
public class AuthFilter implements Filter {

    /** 会话中保存用户对象的键名 */
    public static final String SESSION_USER = "user";

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        User user = (User) req.getSession().getAttribute(SESSION_USER);
        if (user == null) {
            String query = req.getQueryString();
            String current = req.getRequestURI() + (query == null ? "" : ("?" + query));
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" +
                    java.net.URLEncoder.encode(current, "UTF-8"));
            return;
        }
        if (user.isBanned()) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?banned=1");
            return;
        }
        chain.doFilter(req, resp);
    }

    public void destroy() {
    }
}
