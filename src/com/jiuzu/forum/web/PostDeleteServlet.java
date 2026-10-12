package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 删除帖子控制器
 *
 * <p>作者本人与管理员可以删除，会级联清除回复与点赞记录。</p>
 *
 * @author 第九小组
 */
public class PostDeleteServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private PostService postService = new PostService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        int id = getIntParam(req, "id", 0);
        try {
            postService.delete(id, user);
        } catch (IllegalArgumentException | SecurityException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        redirect(resp, req, "/posts");
    }
}
