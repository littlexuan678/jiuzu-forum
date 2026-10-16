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
 * 编辑帖子控制器
 *
 * <ul>
 *   <li>GET  /post/edit?id=x 展示编辑表单（作者本人或管理员）</li>
 *   <li>POST /post/edit 保存修改，支持重新上传配图 / 移除原图</li>
 * </ul>
 *
 * @author 第九小组
 */
public class PostEditServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private PostService postService = new PostService();
    private BoardService boardService = new BoardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        int id = getIntParam(req, "id", 0);
        Post post = postService.findById(id);
        if (post == null) {
            req.getSession().setAttribute("flashError", "帖子不存在或已被删除");
            redirect(resp, req, "/posts");
            return;
        }
        boolean owner = post.getUserId() != null && post.getUserId().intValue() == user.getId();
        if (!owner && !user.isAdmin()) {
            req.getSession().setAttribute("flashError", "只能编辑自己的帖子");
            redirect(resp, req, "/post/detail?id=" + id);
            return;
        }
        List<Board> boards = boardService.listAll();
        req.setAttribute("boards", boards);
        req.setAttribute("boardId", post.getBoardId());
        req.setAttribute("post", post);
        req.setAttribute("editing", Boolean.TRUE);
        forward(req, resp, "post/form.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        boolean mp = isMultipart(req);
        int id = parseInt(mp ? getMultipartString(req, "id") : getStringParam(req, "id"), 0);

        Post post = new Post();
        post.setBoardId(parseInt(mp ? getMultipartString(req, "boardId") : getStringParam(req, "boardId"), 0));
        post.setTitle(mp ? getMultipartString(req, "title") : getStringParam(req, "title"));
        post.setContent(mp ? getMultipartString(req, "content") : getStringParam(req, "content"));

        // 配图：null=不改，空串=移除，非空=换新图
        String imagePath = null;
        if (mp) {
            String remove = getMultipartString(req, "removeImage");
            String uploaded = saveMultipartImage(req, "image", "posts");
            if (uploaded != null && !uploaded.isEmpty()) {
                imagePath = uploaded;
            } else if ("1".equals(remove)) {
                imagePath = "";
            }
        }

        // 附件：整体覆盖；勾选「移除附件」或重新上传了新的，都以当前隐藏域为准
        String removeAttach = mp ? getMultipartString(req, "removeAttachment") : getStringParam(req, "removeAttachment");
        if (!"1".equals(removeAttach)) {
            post.setAttachmentUrl(mp ? getMultipartString(req, "attachmentUrl") : getStringParam(req, "attachmentUrl"));
            post.setAttachmentName(mp ? getMultipartString(req, "attachmentName") : getStringParam(req, "attachmentName"));
            post.setAttachmentSize(mp ? getMultipartString(req, "attachmentSize") : getStringParam(req, "attachmentSize"));
        }

        try {
            postService.update(id, post, user, imagePath);
            redirect(resp, req, "/post/detail?id=" + id);
        } catch (IllegalArgumentException | SecurityException e) {
            Post old = postService.findById(id);
            setError(req, e.getMessage());
            List<Board> boards = boardService.listAll();
            req.setAttribute("boards", boards);
            req.setAttribute("boardId", post.getBoardId());
            req.setAttribute("post", old == null ? post : old);
            req.setAttribute("editing", Boolean.TRUE);
            forward(req, resp, "post/form.jsp");
        }
    }
}
