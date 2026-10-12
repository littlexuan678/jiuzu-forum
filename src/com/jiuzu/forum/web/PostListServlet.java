package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.Board;
import com.jiuzu.forum.bean.PageBean;
import com.jiuzu.forum.bean.Post;
import com.jiuzu.forum.dao.UserDao;
import com.jiuzu.forum.service.BoardService;
import com.jiuzu.forum.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 帖子列表控制器（首页）
 *
 * <p>支持板块筛选、关键词搜索、最新/热门排序与分页，
 * 一次请求把列表数据、板块列表、热门帖与站点统计都准备好交给 JSP 渲染。</p>
 *
 * @author 第九小组
 */
public class PostListServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private PostService postService = new PostService();
    private BoardService boardService = new BoardService();
    private UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int boardId = getIntParam(req, "boardId", 0);
        String keyword = getStringParam(req, "kw");
        String sort = getStringParam(req, "sort");
        if (!"hot".equals(sort)) {
            sort = "new";
        }
        int pageNo = getIntParam(req, "pageNo", 1);

        PageBean<Post> pageBean = postService.list(boardId, keyword, sort, pageNo);
        List<Board> boards = boardService.listAll();
        List<Post> hotPosts = postService.hot(5);

        Board board = boardId > 0 ? boardService.getById(boardId) : null;
        String pageTitle = board != null ? board.getName() : "全部帖子";
        if (!keyword.isEmpty()) {
            pageTitle = "搜索：" + keyword;
        }

        req.setAttribute("pageBean", pageBean);
        req.setAttribute("boards", boards);
        req.setAttribute("hotPosts", hotPosts);
        req.setAttribute("board", board);
        req.setAttribute("boardId", boardId);
        req.setAttribute("kw", keyword);
        req.setAttribute("sort", sort);
        req.setAttribute("pageTitle", pageTitle);
        req.setAttribute("totalPosts", postService.totalCount());
        req.setAttribute("totalUsers", userDao.count());
        req.setAttribute("welcome", getStringParam(req, "welcome"));

        forward(req, resp, "post/list.jsp");
    }
}
