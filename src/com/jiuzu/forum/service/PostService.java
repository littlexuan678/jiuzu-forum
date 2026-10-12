package com.jiuzu.forum.service;

import com.jiuzu.forum.bean.PageBean;
import com.jiuzu.forum.bean.Post;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.PostDao;
import com.jiuzu.forum.dao.ReplyDao;

import java.util.List;

/**
 * 帖子业务逻辑层
 *
 * <p>负责分页列表、搜索、详情浏览、发帖权限校验、点赞、删除与置顶等业务规则。</p>
 *
 * @author 第九小组
 */
public class PostService {

    private PostDao postDao = new PostDao();
    private ReplyDao replyDao = new ReplyDao();
    private BoardService boardService = new BoardService();

    /**
     * 分页查询帖子列表
     *
     * @param boardId 板块 ID（0 表示全部）
     * @param keyword 搜索关键词
     * @param sort    排序：new / hot
     * @param pageNo  页码（从 1 开始）
     * @return 分页对象 PageBean&lt;Post&gt;
     */
    public PageBean<Post> list(int boardId, String keyword, String sort, int pageNo) {
        PageBean<Post> pageBean = new PageBean<Post>();
        pageBean.setPageNo(pageNo);
        int total = postDao.countList(boardId, keyword);
        pageBean.setTotalCount(total);
        int start = (pageBean.getPageNo() - 1) * pageBean.getPageSize();
        List<Post> posts = postDao.findList(boardId, keyword, sort, start, pageBean.getPageSize());
        pageBean.setList(posts);
        return pageBean;
    }

    /**
     * 查询帖子详情，并累加浏览量；会标记当前用户是否点过赞
     *
     * @param id     帖子 ID
     * @param viewer 当前登录用户，可为 null
     * @return 帖子对象
     */
    public Post detail(int id, User viewer) {
        Post post = postDao.findById(id);
        if (post == null) {
            return null;
        }
        postDao.increaseViews(id);
        post.setViews(post.getViews() + 1);
        if (viewer != null) {
            post.setLiked(postDao.hasLiked(id, viewer.getId()));
        }
        return post;
    }

    /**
     * 发布新帖
     *
     * @param post 帖子对象（含板块、标题、正文）
     * @param user 发帖人
     */
    public void create(Post post, User user) {
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (post.getTitle().length() > 60) {
            throw new IllegalArgumentException("标题不能超过 60 个字");
        }
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("正文不能为空");
        }
        // 公告区仅管理员可发
        if (boardService.isAnnouncementBoard(post.getBoardId()) && !user.isAdmin()) {
            throw new SecurityException("公告区只有管理员可以发帖");
        }
        post.setUserId(user.getId());
        post.setPinned(0);
        postDao.insert(post);
    }

    /**
     * 删除帖子：作者本人或管理员可删，级联删除回复与点赞
     *
     * @param id   帖子 ID
     * @param user 操作人
     */
    public void delete(int id, User user) {
        Post post = postDao.findById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        boolean owner = post.getUserId() != null && post.getUserId().intValue() == user.getId();
        if (!owner && !user.isAdmin()) {
            throw new SecurityException("只能删除自己的帖子");
        }
        replyDao.deleteByPostId(id);
        postDao.deleteLikesByPostId(id);
        postDao.delete(id);
    }

    /**
     * 点赞 / 取消点赞
     *
     * @param postId 帖子 ID
     * @param user   当前用户
     * @return true 表示点赞成功，false 表示已取消
     */
    public boolean toggleLike(int postId, User user) {
        if (postDao.hasLiked(postId, user.getId())) {
            postDao.removeLike(postId, user.getId());
            postDao.syncLikes(postId);
            return false;
        }
        postDao.addLike(postId, user.getId());
        postDao.syncLikes(postId);
        return true;
    }

    /** 热门帖 TopN */
    public List<Post> hot(int limit) {
        return postDao.findHot(limit);
    }

    /** 置顶 / 取消置顶（管理员） */
    public void pin(int id, boolean pin, User operator) {
        checkAdmin(operator);
        postDao.updatePinned(id, pin ? 1 : 0);
    }

    /** 管理员删除任意帖子 */
    public void deleteByAdmin(int id, User operator) {
        checkAdmin(operator);
        replyDao.deleteByPostId(id);
        postDao.deleteLikesByPostId(id);
        postDao.delete(id);
    }

    /** 站点帖子总数 */
    public int totalCount() {
        return postDao.count();
    }

    /** 校验管理员身份 */
    private void checkAdmin(User user) {
        if (user == null || !user.isAdmin()) {
            throw new SecurityException("需要管理员权限");
        }
    }
}
