package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.Reply;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.ReplyDao;
import com.jiuzu.forum.service.ReplyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 删除回复控制器
 *
 * <p>回复者本人与管理员可删，删除后回到帖子详情页。</p>
 *
 * @author 第九小组
 */
public class ReplyDeleteServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private ReplyService replyService = new ReplyService();
    private ReplyDao replyDao = new ReplyDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        int id = getIntParam(req, "id", 0);
        Reply reply = replyDao.findById(id);
        int postId = reply != null ? reply.getPostId() : 0;
        try {
            replyService.delete(id, user);
        } catch (IllegalArgumentException | SecurityException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        if (postId > 0) {
            redirect(resp, req, "/post/detail?id=" + postId);
        } else {
            redirect(resp, req, "/posts");
        }
    }
}
