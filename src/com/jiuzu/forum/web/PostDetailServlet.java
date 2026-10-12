package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.Post;
import com.jiuzu.forum.bean.Reply;
import com.jiuzu.forum.service.PostService;
import com.jiuzu.forum.service.ReplyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 帖子详情控制器
 *
 * <p>加载帖子正文、回复列表并累加浏览量。</p>
 *
 * @author 第九小组
 */
public class PostDetailServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private PostService postService = new PostService();
    private ReplyService replyService = new ReplyService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = getIntParam(req, "id", 0);
        Post post = postService.detail(id, getLoginUser(req));
        if (post == null) {
            req.setAttribute("message", "帖子不存在或已被删除");
            forward(req, resp, "error/404.jsp");
            return;
        }
        List<Reply> replies = replyService.listByPostId(id);
        req.setAttribute("post", post);
        req.setAttribute("replies", replies);
        req.setAttribute("pageTitle", post.getTitle());
        forward(req, resp, "post/detail.jsp");
    }
}
