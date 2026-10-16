package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.Board;
import com.jiuzu.forum.bean.PageBean;
import com.jiuzu.forum.bean.Post;
import com.jiuzu.forum.bean.Reply;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.BoardDao;
import com.jiuzu.forum.dao.PostDao;
import com.jiuzu.forum.dao.ReplyDao;
import com.jiuzu.forum.dao.UserDao;
import com.jiuzu.forum.service.BoardService;
import com.jiuzu.forum.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * 后台管理控制器
 *
 * <p>管理员专属，包含：</p>
 * <ul>
 *   <li>数据统计：用户 / 帖子 / 回复 / 板块总数与今日新增</li>
 *   <li>内容管理：查看全部帖子、删除、置顶/取消置顶</li>
 *   <li>用户管理：查看、封禁/解封、权限、删除</li>
 *   <li>板块管理：新增 / 编辑 / 删除（删除时可合并帖子到其他板块）</li>
 *   <li>回复管理：查看全部回复、删除不当回复</li>
 * </ul>
 *
 * @author 第九小组
 */
public class AdminServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private UserDao userDao = new UserDao();
    private PostDao postDao = new PostDao();
    private ReplyDao replyDao = new ReplyDao();
    private BoardDao boardDao = new BoardDao();
    private BoardService boardService = new BoardService();
    private PostService postService = new PostService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = getLoginUser(req);
        if (admin == null || !admin.isAdmin()) {
            redirect(resp, req, "/posts");
            return;
        }
        // 标记后台页面：让 header 给 body 加 class，从而启用后台专用的深色实色样式
        req.setAttribute("adminMode", Boolean.TRUE);

        String op = getStringParam(req, "op");
        if (op.isEmpty()) {
            op = "dashboard";
        }
        int pageNo = getIntParam(req, "pageNo", 1);

        if ("dashboard".equals(op)) {
            req.setAttribute("statUsers", userDao.count());
            req.setAttribute("statPosts", postDao.count());
            req.setAttribute("statReplies", replyDao.countAll());
            List<Board> boards = boardService.listAll();
            req.setAttribute("statBoards", boards.size());
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            req.setAttribute("statNewPostsToday", postDao.countSince(c.getTimeInMillis()));
            req.setAttribute("pageTitle", "数据统计");
            forward(req, resp, "admin/dashboard.jsp");
            return;
        }

        if ("boards".equals(op)) {
            req.setAttribute("boards", boardService.listAll());
            req.setAttribute("pageTitle", "板块管理");
            forward(req, resp, "admin/boards.jsp");
            return;
        }

        if ("replies".equals(op)) {
            PageBean<Reply> pageBean = new PageBean<Reply>();
            pageBean.setPageNo(pageNo);
            pageBean.setPageSize(20);
            pageBean.setTotalCount(replyDao.countAll());
            int start = (pageBean.getPageNo() - 1) * pageBean.getPageSize();
            pageBean.setList(replyDao.findList(start, pageBean.getPageSize()));
            req.setAttribute("pageBean", pageBean);
            req.setAttribute("pageTitle", "回复管理");
            forward(req, resp, "admin/replies.jsp");
            return;
        }

        if ("users".equals(op)) {
            PageBean<User> pageBean = new PageBean<User>();
            pageBean.setPageNo(pageNo);
            pageBean.setPageSize(15);
            pageBean.setTotalCount(userDao.count());
            int start = (pageBean.getPageNo() - 1) * pageBean.getPageSize();
            pageBean.setList(userDao.findByPage(start, pageBean.getPageSize()));
            req.setAttribute("pageBean", pageBean);
            req.setAttribute("op", op);
            req.setAttribute("pageTitle", "用户管理");
            forward(req, resp, "admin/users.jsp");
            return;
        }

        // 默认：内容管理
        PageBean<Post> pageBean = new PageBean<Post>();
        pageBean.setPageNo(pageNo);
        pageBean.setPageSize(15);
        pageBean.setTotalCount(postDao.countList(0, null));
        int start = (pageBean.getPageNo() - 1) * pageBean.getPageSize();
        pageBean.setList(postDao.findList(0, null, "new", start, pageBean.getPageSize()));
        req.setAttribute("pageBean", pageBean);
        req.setAttribute("op", op);
        List<Board> boards = boardService.listAll();
        req.setAttribute("boards", boards);
        req.setAttribute("pageTitle", "内容管理");
        forward(req, resp, "admin/posts.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = getLoginUser(req);
        if (admin == null || !admin.isAdmin()) {
            redirect(resp, req, "/posts");
            return;
        }
        String action = getStringParam(req, "action");
        int id = getIntParam(req, "id", 0);
        String back = getStringParam(req, "op");
        try {
            if ("addBoard".equals(action)) {
                String name = getStringParam(req, "name");
                if (name.isEmpty()) {
                    req.getSession().setAttribute("flashError", "板块名称不能为空");
                } else {
                    boardService.create(name, getStringParam(req, "icon"),
                            getStringParam(req, "description"), getIntParam(req, "sortOrder", 0));
                    req.getSession().setAttribute("flash", "板块已添加");
                }
            } else if ("editBoard".equals(action)) {
                boardService.update(id, getStringParam(req, "name"),
                        getStringParam(req, "icon"), getStringParam(req, "description"),
                        getIntParam(req, "sortOrder", 0));
                req.getSession().setAttribute("flash", "板块已更新");
            } else if ("delBoard".equals(action)) {
                if (boardService.listAll().size() <= 1) {
                    req.getSession().setAttribute("flashError", "至少保留一个板块，无法删除");
                } else {
                    int mergeTo = getIntParam(req, "mergeTo", 0);
                    boardService.deleteBoard(id, mergeTo);
                    req.getSession().setAttribute("flash", mergeTo > 0 && mergeTo != id
                            ? "板块已删除，帖子已合并到目标板块" : "板块及其帖子已删除");
                }
            } else if ("delReply".equals(action)) {
                // 楼中楼：删父级时把挂在它下面的子回复一起清掉
                replyDao.deleteByParentId(id);
                replyDao.delete(id);
                req.getSession().setAttribute("flash", "回复已删除");
            } else if ("pin".equals(action)) {
                postService.pin(id, getIntParam(req, "on", 1) == 1, admin);
                req.getSession().setAttribute("flash", "置顶状态已更新");
            } else if ("delPost".equals(action)) {
                postService.deleteByAdmin(id, admin);
                req.getSession().setAttribute("flash", "帖子已删除");
            } else if ("ban".equals(action)) {
                userDao.updateBanned(id, 1);
                req.getSession().setAttribute("flash", "用户已封禁");
            } else if ("unban".equals(action)) {
                userDao.updateBanned(id, 0);
                req.getSession().setAttribute("flash", "用户已解封");
            } else if ("role".equals(action)) {
                userDao.updateRole(id, getIntParam(req, "role", 0));
                req.getSession().setAttribute("flash", "角色已更新");
            } else if ("delUser".equals(action)) {
                if (id == admin.getId()) {
                    req.getSession().setAttribute("flashError", "不能删除当前登录的管理员");
                } else {
                    replyDao.deleteByUserId(id);
                    postDao.deleteLikesByPostId(id);
                    userDao.delete(id);
                    req.getSession().setAttribute("flash", "用户已删除（其回复也已清理）");
                }
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        redirect(resp, req, "/admin?op=" + (back.isEmpty() ? "dashboard" : back));
    }
}
