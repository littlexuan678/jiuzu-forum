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
        req.setAttribute("boards", boards);
        req.setAttribute("boardId", getIntParam(req, "boardId", boards.isEmpty() ? 0 : boards.get(0).getId()));
        forward(req, resp, "post/form.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        boolean mp = isMultipart(req);
        Post post = new Post();
        post.setBoardId(parseInt(mp ? getMultipartString(req, "boardId") : getStringParam(req, "boardId"), 0));
        post.setTitle(mp ? getMultipartString(req, "title") : getStringParam(req, "title"));
        post.setContent(mp ? getMultipartString(req, "content") : getStringParam(req, "content"));

        String imagePath = null;
        if (mp) {
            try {
                imagePath = saveMultipartImage(req, "image", "posts");
            } catch (IllegalArgumentException e) {
                setError(req, e.getMessage());
                showForm(req, resp, post.getBoardId());
                return;
            }
        }

        req.setAttribute("post", post);
        try {
            postService.create(post, user, imagePath);
            // 用 doesSession (Flash) 方式提示，这里简化为重定向到最新列表
            redirect(resp, req, "/posts?sort=new");
        } catch (IllegalArgumentException | SecurityException e) {
            setError(req, e.getMessage());
            showForm(req, resp, post.getBoardId());
        }
    }

    /** 回填板块列表并回到发帖表单（校验失败时保留已填内容） */
    private void showForm(HttpServletRequest req, HttpServletResponse resp, int boardId)
            throws ServletException, IOException {
        List<Board> boards = boardService.listAll();
        req.setAttribute("boards", boards);
        req.setAttribute("boardId", boardId);
        forward(req, resp, "post/form.jsp");
    }
}
