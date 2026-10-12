package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.Board;
import com.jiuzu.forum.bean.PageBean;
import com.jiuzu.forum.bean.Post;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.PostDao;
import com.jiuzu.forum.dao.ReplyDao;
import com.jiuzu.forum.dao.UserDao;
import com.jiuzu.forum.service.BoardService;
import com.jiuzu.forum.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 后台管理控制器
 *
 * <p>管理员专属，包含：</p>
 * <ul>
 *   <li>用户管理：查看、封禁/解封、删除用户（体现单表数据管理）</li>
 *   <li>内容管理：查看全部帖子、删除、置顶/取消置顶</li>
 * </ul>
 *
 * @author 第九小组
 */
public class AdminServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private UserDao userDao = new UserDao();
    private PostDao postDao = new PostDao();
    private ReplyDao replyDao = new ReplyDao();
    private PostService postService = new PostService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = getLoginUser(req);
        if (admin == null || !admin.isAdmin()) {
            redirect(resp, req, "/posts");
            return;
        }
        String op = getStringParam(req, "op");
        if (op.isEmpty()) {
            op = "posts";
        }
        int pageNo = getIntParam(req, "pageNo", 1);

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

        PageBean<Post> pageBean = new PageBean<Post>();
        pageBean.setPageNo(pageNo);
        pageBean.setPageSize(15);
        pageBean.setTotalCount(postDao.countList(0, null));
        int start = (pageBean.getPageNo() - 1) * pageBean.getPageSize();
        pageBean.setList(postDao.findList(0, null, "new", start, pageBean.getPageSize()));
        req.setAttribute("pageBean", pageBean);
        req.setAttribute("op", op);
        List<Board> boards = new BoardService().listAll();
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
            if ("pin".equals(action)) {
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
        redirect(resp, req, "/admin?op=" + (back.isEmpty() ? "posts" : back));
    }
}
