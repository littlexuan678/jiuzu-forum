package com.jiuzu.forum.dao;

import com.jiuzu.forum.bean.Reply;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 回复表（t_reply）数据访问对象
 *
 * @author 第九小组
 */
public class ReplyDao extends BaseDao {

    /** 联表查询：回复 + 回复者 */
    private static final String SELECT_REPLY =
            "SELECT r.*, u.nickname, u.avatar_color FROM t_reply r " +
                    "LEFT JOIN t_user u ON u.id=r.user_id ";

    private static final RowMapper<Reply> REPLY_MAPPER = new RowMapper<Reply>() {
        public Reply mapRow(ResultSet rs) throws SQLException {
            Reply r = new Reply();
            r.setId(rs.getInt("id"));
            r.setPostId(rs.getInt("post_id"));
            r.setUserId(rs.getInt("user_id"));
            r.setContent(rs.getString("content"));
            r.setCreatedTime(rs.getLong("created_time"));
            r.setNickname(rs.getString("nickname"));
            r.setAvatarColor(rs.getString("avatar_color"));
            return r;
        }
    };

    /** 查询某个帖子的全部回复（按时间正序） */
    public List<Reply> findByPostId(int postId) {
        String sql = SELECT_REPLY + " WHERE r.post_id=? ORDER BY r.created_time ASC";
        return query(sql, new Object[]{postId}, REPLY_MAPPER);
    }

    /** 单条回复 */
    public Reply findById(int id) {
        return queryOne(SELECT_REPLY + " WHERE r.id=?", new Object[]{id}, REPLY_MAPPER);
    }

    /** 新增回复 */
    public int insert(Reply reply) {
        String sql = "INSERT INTO t_reply(post_id,user_id,content,created_time) VALUES(?,?,?,?)";
        return update(sql, new Object[]{reply.getPostId(), reply.getUserId(),
                reply.getContent(), System.currentTimeMillis()});
    }

    /** 删除回复 */
    public int delete(int id) {
        return update("DELETE FROM t_reply WHERE id=?", new Object[]{id});
    }

    /** 删除某个帖子下的全部回复（删帖时调用） */
    public int deleteByPostId(int postId) {
        return update("DELETE FROM t_reply WHERE post_id=?", new Object[]{postId});
    }

    /** 删除某个用户的全部回复（删除用户时调用） */
    public int deleteByUserId(int userId) {
        return update("DELETE FROM t_reply WHERE user_id=?", new Object[]{userId});
    }

    /** 统计回复数 */
    public int countByPostId(int postId) {
        return count("SELECT COUNT(1) FROM t_reply WHERE post_id=?", new Object[]{postId});
    }
}
