package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 点赞控制器
 *
 * <p>同一个用户对同一帖子只能点赞一次，再次请求即为取消点赞。</p>
 *
 * @author 第九小组
 */
public class LikeServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private PostService postService = new PostService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        int id = getIntParam(req, "id", 0);
        postService.toggleLike(id, user);
        redirect(resp, req, "/post/detail?id=" + id);
    }
}
