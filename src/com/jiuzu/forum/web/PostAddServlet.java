package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.Board;
import com.jiuzu.forum.bean.Post;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.service.BoardService;
import com.jiuzu.forum.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 发帖控制器
 *
 * <ul>
 *   <li>GET  /post/add 展示发帖表单（公告区对普通用户隐藏）</li>
 *   <li>POST /post/add 保存新帖，成功后跳到详情页</li>
 * </ul>
 *
 * @author 第九小组
 */
public class PostAddServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private PostService postService = new PostService();
    private BoardService boardService = new BoardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        List<Board> boards = boardService.listAll();
        List<Board> visible = new ArrayList<Board>();
        for (Board b : boards) {
            // 公告区只允许管理员发帖
            if ("公告区".equals(b.getName()) && !user.isAdmin()) {
                continue;
            }
            visible.add(b);
        }
        req.setAttribute("boards", visible);
        req.setAttribute("boardId", getIntParam(req, "boardId", visible.isEmpty() ? 0 : visible.get(0).getId()));
        forward(req, resp, "post/form.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        Post post = new Post();
        post.setBoardId(getIntParam(req, "boardId", 0));
        post.setTitle(getStringParam(req, "title"));
        post.setContent(getStringParam(req, "content"));

        req.setAttribute("post", post);
        try {
            postService.create(post, user);
            // 用 doesSession (Flash) 方式提示，这里简化为重定向到最新列表
            redirect(resp, req, "/posts?sort=new");
        } catch (IllegalArgumentException | SecurityException e) {
            setError(req, e.getMessage());
            List<Board> boards = boardService.listAll();
            req.setAttribute("boards", boards);
            req.setAttribute("boardId", post.getBoardId());
            forward(req, resp, "post/form.jsp");
        }
    }
}
