package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.service.ReplyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 发表回复控制器
 *
 * @author 第九小组
 */
public class ReplyAddServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private ReplyService replyService = new ReplyService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        int postId = getIntParam(req, "postId", 0);
        String content = getStringParam(req, "content");
        try {
            replyService.create(postId, content, user);
        } catch (IllegalArgumentException | SecurityException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        redirect(resp, req, "/post/detail?id=" + postId);
    }
}
