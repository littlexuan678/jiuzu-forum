package com.jiuzu.forum.web;

import com.jiuzu.forum.filter.AuthFilter;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 注册控制器
 *
 * <ul>
 *   <li>GET  /register 展示注册表单</li>
 *   <li>POST /register 校验并写入数据库，注册成功后自动登录</li>
 * </ul>
 *
 * @author 第九小组
 */
public class RegisterServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (getLoginUser(req) != null) {
            redirect(resp, req, "/posts");
            return;
        }
        req.setAttribute("mode", "register");
        forward(req, resp, "auth/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = getStringParam(req, "username");
        String password = getStringParam(req, "password");
        String confirm = getStringParam(req, "confirm");
        String nickname = getStringParam(req, "nickname");

        // 页面保留用户已填内容，出错时不用重新输入
        req.setAttribute("username", username);
        req.setAttribute("nickname", nickname);
        req.setAttribute("mode", "register");

        if (isEmpty(username) || isEmpty(password)) {
            setError(req, "用户名和密码不能为空");
            forward(req, resp, "auth/login.jsp");
            return;
        }
        if (!password.equals(confirm)) {
            setError(req, "两次输入的密码不一致");
            forward(req, resp, "auth/login.jsp");
            return;
        }

        try {
            User user = userService.register(username, password, nickname);
            // 注册成功直接建立会话，避免用户再手动登录一次
            req.getSession().setAttribute(AuthFilter.SESSION_USER, user);
            redirect(resp, req, "/posts?welcome=1");
        } catch (IllegalArgumentException e) {
            setError(req, e.getMessage());
            forward(req, resp, "auth/login.jsp");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
