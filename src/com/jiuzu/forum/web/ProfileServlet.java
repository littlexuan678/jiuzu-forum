package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.PostDao;
import com.jiuzu.forum.dao.ReplyDao;
import com.jiuzu.forum.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 个人中心控制器
 *
 * <ul>
 *   <li>GET  /user/profile 展示个人资料页</li>
 *   <li>POST /user/profile?action=updateInfo 改昵称与头像</li>
 *   <li>POST /user/profile?action=updatePwd  改密码（校验原密码）</li>
 * </ul>
 *
 * <p>头像图片由前端先调用 /upload 拿到地址，再把地址随表单提交过来保存。</p>
 *
 * @author 第九小组
 */
public class ProfileServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();
    private PostDao postDao = new PostDao();
    private ReplyDao replyDao = new ReplyDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User login = requireLogin(req);
        User fresh = userService.getById(login.getId());
        if (fresh != null) {
            fresh.setPassword(null);
            req.getSession().setAttribute("user", fresh);
            req.setAttribute("user", fresh);
        } else {
            req.setAttribute("user", login);
        }
        // 我的发帖数 / 回复数
        req.setAttribute("myPosts", postDao.countByUser(login.getId()));
        req.setAttribute("myReplies", replyDao.countByUser(login.getId()));

        String msg = getStringParam(req, "msg");
        if ("info".equals(msg)) {
            setMessage(req, "个人资料已更新");
        } else if ("pwd".equals(msg)) {
            setMessage(req, "密码修改成功，下次登录请用新密码");
        }
        forward(req, resp, "user/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User login = requireLogin(req);
        String action = getStringParam(req, "action");

        if ("updatePwd".equals(action)) {
            String oldPwd = getStringParam(req, "oldPwd");
            String newPwd = getStringParam(req, "newPwd");
            String confirm = getStringParam(req, "confirmPwd");
            try {
                if (confirm == null || !confirm.equals(newPwd)) {
                    throw new IllegalArgumentException("两次输入的新密码不一致");
                }
                User current = userService.getById(login.getId());
                userService.changePassword(login.getId(), oldPwd, newPwd, current);
                refreshSession(req, login.getId());
                redirect(resp, req, "/user/profile?msg=pwd");
            } catch (IllegalArgumentException | SecurityException e) {
                setError(req, e.getMessage());
                showProfile(req, resp, login);
            }
            return;
        }

        // 默认：更新昵称 + 头像
        String nickname = getStringParam(req, "nickname");
        String avatar = getStringParam(req, "avatar");
        try {
            userService.updateProfile(login.getId(), nickname, avatar);
            refreshSession(req, login.getId());
            redirect(resp, req, "/user/profile?msg=info");
        } catch (IllegalArgumentException e) {
            setError(req, e.getMessage());
            showProfile(req, resp, login);
        }
    }

    /** 把最新的用户信息写回 Session，保证全站昵称 / 头像立即生效 */
    private void refreshSession(HttpServletRequest req, int userId) {
        User fresh = userService.getById(userId);
        if (fresh != null) {
            fresh.setPassword(null);
            req.getSession().setAttribute("user", fresh);
        }
    }

    /** 校验失败时回填资料页 */
    private void showProfile(HttpServletRequest req, HttpServletResponse resp, User login)
            throws ServletException, IOException {
        User fresh = userService.getById(login.getId());
        req.setAttribute("user", fresh == null ? login : fresh);
        forward(req, resp, "user/profile.jsp");
    }
}
