package com.jiuzu.forum.service;

import com.jiuzu.forum.bean.Reply;
import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.PostDao;
import com.jiuzu.forum.dao.ReplyDao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 回复业务逻辑层
 *
 * @author 第九小组
 */
public class ReplyService {

    private ReplyDao replyDao = new ReplyDao();
    private PostDao postDao = new PostDao();

    /** 查询某帖下的回复（扁平列表，含楼中楼子回复） */
    public List<Reply> listFlatByPostId(int postId) {
        return replyDao.findByPostId(postId);
    }

    /**
     * 查询某帖下的回复，并组装成「楼中楼」两层结构：
     * 顶层回复按时间正序，每条顶层回复的 {@code subReplies} 里挂它的子回复。
     */
    public List<Reply> listByPostId(int postId) {
        return buildTree(replyDao.findByPostId(postId));
    }

    /** 把扁平列表按 parent_id 组装成两层树；父级不存在或指向自己时降级为顶层楼层 */
    private List<Reply> buildTree(List<Reply> all) {
        Map<Integer, Reply> index = new LinkedHashMap<Integer, Reply>();
        for (Reply r : all) {
            r.setSubReplies(new ArrayList<Reply>());
            index.put(r.getId(), r);
        }
        List<Reply> roots = new ArrayList<Reply>();
        for (Reply r : all) {
            Integer pid = r.getParentId();
            if (pid != null && pid > 0 && !pid.equals(r.getId())) {
                Reply parent = index.get(pid);
                if (parent != null) {
                    parent.getSubReplies().add(r);
                    continue;
                }
            }
            roots.add(r);
        }
        return roots;
    }

    /**
     * 发表回复
     *
     * @param postId    帖子 ID
     * @param content   回复内容（可为空，但需至少带一张图片）
     * @param user      回复者
     * @param imagePath 回复附带的图片相对路径，无图传 null
     */
    public void create(int postId, String content, User user, String imagePath) {
        create(postId, content, user, imagePath, null, null);
    }

    /**
     * 发表回复（带发布 IP 与属地）
     *
     * @param postId    帖子 ID
     * @param content   回复内容（可为空，但需至少带一张图片）
     * @param user      回复者
     * @param imagePath 回复附带的图片相对路径，无图传 null
     * @param ip        客户端 IP，可为 null
     * @param ipLoc     IP 属地文案，可为 null
     */
    public void create(int postId, String content, User user, String imagePath, String ip, String ipLoc) {
        create(postId, content, user, imagePath, ip, ipLoc, 0);
    }

    /**
     * 发表回复（带发布 IP、属地与楼中楼父级）
     *
     * @param parentId 被回复的那条回复 ID，0 表示顶层楼层。
     *                 若被回复的回复本身也是楼中楼，会自动挂到它所属的顶层楼层下（保持两层，不再套娃）。
     */
    public void create(int postId, String content, User user, String imagePath,
                       String ip, String ipLoc, int parentId) {
        if (postDao.findById(postId) == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        boolean hasText = content != null && !content.trim().isEmpty();
        boolean hasImage = imagePath != null && !imagePath.isEmpty();
        if (!hasText && !hasImage) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        if (hasText && content.length() > 1000) {
            throw new IllegalArgumentException("回复内容过长（最多 1000 字）");
        }

        // 楼中楼：校验父级存在且同帖，昵称一律以库里为准（不接受客户端传入，避免伪造）
        int realParentId = 0;
        String replyToUser = null;
        if (parentId > 0) {
            Reply parent = replyDao.findById(parentId);
            if (parent == null || parent.getPostId() == null || parent.getPostId().intValue() != postId) {
                throw new IllegalArgumentException("被回复的内容不存在或已被删除");
            }
            // 只保留两层：回复「楼中楼里的某条」时，挂到它所属的顶层楼层
            Integer grand = parent.getParentId();
            realParentId = (grand != null && grand > 0) ? grand : parent.getId();
            replyToUser = parent.getNickname();
        }

        Reply reply = new Reply();
        reply.setPostId(postId);
        reply.setUserId(user.getId());
        reply.setContent(hasText ? content.trim() : "");
        if (hasImage) {
            reply.setImagePath(imagePath);
        }
        reply.setIp(ip);
        reply.setIpLoc(ipLoc);
        reply.setParentId(realParentId);
        reply.setReplyToUser(replyToUser);
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
        // 楼中楼：删顶层楼层时把挂在它下面的子回复一起清掉，避免留下孤儿数据
        replyDao.deleteByParentId(replyId);
        replyDao.delete(replyId);
    }

    /** 统计回复数 */
    public int countByPostId(int postId) {
        return replyDao.countByPostId(postId);
    }
}
