package com.jiuzu.forum.service;

import com.jiuzu.forum.bean.Board;
import com.jiuzu.forum.bean.Post;
import com.jiuzu.forum.dao.BoardDao;
import com.jiuzu.forum.dao.PostDao;
import com.jiuzu.forum.dao.ReplyDao;

import java.util.List;

/**
 * 板块业务逻辑层
 *
 * @author 第九小组
 */
public class BoardService {

    private BoardDao boardDao = new BoardDao();
    private PostDao postDao = new PostDao();
    private ReplyDao replyDao = new ReplyDao();

    /** 查询全部板块（含帖子数） */
    public List<Board> listAll() {
        return boardDao.findAllWithCount();
    }

    /** 根据 ID 查询板块 */
    public Board getById(int id) {
        return boardDao.findById(id);
    }

    /** 新增板块 */
    public void create(String name, String icon, String description, int sortOrder) {
        boardDao.insert(new Board(name, icon, description, sortOrder));
    }

    /** 更新板块信息 */
    public void update(int id, String name, String icon, String description, int sortOrder) {
        Board b = boardDao.findById(id);
        if (b == null) {
            return;
        }
        b.setName(name);
        b.setIcon(icon);
        b.setDescription(description);
        b.setSortOrder(sortOrder);
        boardDao.update(b);
    }

    /** 统计某板块下的帖子数 */
    public int countPosts(int boardId) {
        return boardDao.countPosts(boardId);
    }

    /** 把帖子从一个板块迁移到另一个板块 */
    public void movePosts(int fromBoardId, int toBoardId) {
        boardDao.movePosts(fromBoardId, toBoardId);
    }

    /**
     * 删除板块。
     *
     * @param id      待删除板块 ID
     * @param mergeTo 合并目标板块 ID：>0 且不等于自身时，把帖子并入目标板块；
     *                否则连同该板块下的帖子、回复、点赞一并删除
     */
    public void deleteBoard(int id, int mergeTo) {
        if (mergeTo > 0 && mergeTo != id) {
            boardDao.movePosts(id, mergeTo);
        } else {
            List<Post> posts = postDao.findByBoard(id);
            for (Post p : posts) {
                replyDao.deleteByPostId(p.getId());
                postDao.deleteLikesByPostId(p.getId());
            }
            postDao.deleteByBoardId(id);
        }
        boardDao.delete(id);
    }
}
