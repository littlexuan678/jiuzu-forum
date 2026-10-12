package com.jiuzu.forum.service;

import com.jiuzu.forum.bean.Reply;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.PostDao;
import com.jiuzu.forum.dao.ReplyDao;

import java.util.List;

/**
 * 回复业务逻辑层
 *
 * @author 第九小组
 */
public class ReplyService {

    private ReplyDao replyDao = new ReplyDao();
    private PostDao postDao = new PostDao();

    /** 查询某帖下的全部回复 */
    public List<Reply> listByPostId(int postId) {
        return replyDao.findByPostId(postId);
    }

    /**
     * 发表回复
     *
     * @param postId  帖子 ID
     * @param content 回复内容
     * @param user    回复者
     */
    public void create(int postId, String content, User user) {
        if (postDao.findById(postId) == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("回复内容过长（最多 1000 字）");
        }
        Reply reply = new Reply();
        reply.setPostId(postId);
        reply.setUserId(user.getId());
        reply.setContent(content.trim());
        replyDao.insert(reply);
    }

    /**
     * 删除回复：回复者本人、帖子作者或管理员可删
     *
     * @param replyId  回复 ID
     * @param operator 操作人
     */
    public void delete(int replyId, User operator) {
        Reply reply = replyDao.findById(replyId);
        if (reply == null) {
            throw new IllegalArgumentException("回复不存在");
        }
        boolean owner = reply.getUserId() != null && reply.getUserId().intValue() == operator.getId();
        if (!owner && !operator.isAdmin()) {
            throw new SecurityException("只能删除自己的回复");
        }
        replyDao.delete(replyId);
    }

    /** 统计回复数 */
    public int countByPostId(int postId) {
        return replyDao.countByPostId(postId);
    }
}
