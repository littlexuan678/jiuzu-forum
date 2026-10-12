package com.jiuzu.forum.web;

import com.jiuzu.forum.filter.AuthFilter;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * 登录控制器
 *
 * <ul>
 *   <li>GET  /login  展示登录页</li>
 *   <li>POST /login  校验用户名密码，成功后写入 Session</li>
 * </ul>
 *
 * @author 第九小组
 */
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 已登录直接进入论坛
        if (getLoginUser(req) != null) {
            redirect(resp, req, "/posts");
            return;
        }
        req.setAttribute("redirect", getStringParam(req, "redirect"));
        req.setAttribute("banned", getStringParam(req, "banned"));
        forward(req, resp, "auth/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = getStringParam(req, "username");
        String password = getStringParam(req, "password");
        String back = getStringParam(req, "redirect");

        try {
            User user = userService.login(username, password);
            if (user == null) {
                setError(req, "用户名或密码不正确");
                req.setAttribute("username", username);
                forward(req, resp, "auth/login.jsp");
                return;
            }
            req.getSession().setAttribute(AuthFilter.SESSION_USER, user);
            if (!back.isEmpty()) {
                resp.sendRedirect(URLDecoder.decode(back, "UTF-8"));
                return;
            }
            redirect(resp, req, "/posts");
        } catch (SecurityException e) {
            setError(req, e.getMessage());
            forward(req, resp, "auth/login.jsp");
        }
    }
}
