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
        boolean mp = isMultipart(req);
        int postId = parseInt(mp ? getMultipartString(req, "postId") : getStringParam(req, "postId"), 0);
        String content = mp ? getMultipartString(req, "content") : getStringParam(req, "content");

        String imagePath = null;
        if (mp) {
            try {
                imagePath = saveMultipartImage(req, "image", "replies");
            } catch (IllegalArgumentException e) {
                req.getSession().setAttribute("flashError", e.getMessage());
                redirect(resp, req, "/post/detail?id=" + postId);
                return;
            }
        }

        try {
            replyService.create(postId, content, user, imagePath);
        } catch (IllegalArgumentException | SecurityException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        redirect(resp, req, "/post/detail?id=" + postId);
    }
}
