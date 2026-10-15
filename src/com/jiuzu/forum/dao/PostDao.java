package com.jiuzu.forum.dao;

import com.jiuzu.forum.bean.Post;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子表（t_post）数据访问对象
 *
 * <p>包含帖子列表分页查询、条件搜索、点赞、浏览数累加等 JDBC 操作。</p>
 *
 * @author 第九小组
 */
public class PostDao extends BaseDao {

    /** 联表查询的基础 SELECT：帖子 + 作者 + 板块 + 回复数 */
    private static final String SELECT_POST =
            "SELECT p.*, u.nickname, u.avatar_color, b.name AS board_name, b.icon AS board_icon, " +
                    "(SELECT COUNT(1) FROM t_reply r WHERE r.post_id=p.id) AS reply_count " +
                    "FROM t_post p " +
                    "LEFT JOIN t_user u ON u.id=p.user_id " +
                    "LEFT JOIN t_board b ON b.id=p.board_id ";

    /** 帖子行映射器 */
    private static final RowMapper<Post> POST_MAPPER = new RowMapper<Post>() {
        public Post mapRow(ResultSet rs) throws SQLException {
            Post p = new Post();
            p.setId(rs.getInt("id"));
            p.setBoardId(rs.getInt("board_id"));
            p.setBoardName(rs.getString("board_name"));
            p.setBoardIcon(rs.getString("board_icon"));
            p.setTitle(rs.getString("title"));
            p.setContent(rs.getString("content"));
            p.setImagePath(rs.getString("image_path"));
            p.setUserId(rs.getInt("user_id"));
            p.setNickname(rs.getString("nickname"));
            p.setAvatarColor(rs.getString("avatar_color"));
            p.setViews(rs.getInt("views"));
            p.setLikes(rs.getInt("likes"));
            p.setPinned(rs.getInt("pinned"));
            p.setCreatedTime(rs.getLong("created_time"));
            p.setUpdatedTime(rs.getLong("updated_time"));
            p.setReplies(rs.getInt("reply_count"));
            return p;
        }
    };

    /**
     * 条件查询帖子列表
     *
     * @param boardId 板块 ID，0 或负数表示不限板块
     * @param keyword 搜索关键词，null 或空表示不限
     * @param sort    排序方式：new 最新 / hot 热门
     * @param start   起始位置
     * @param size    条数
     * @return 帖子列表
     */
    public List<Post> findList(int boardId, String keyword, String sort, int start, int size) {
        StringBuilder sb = new StringBuilder(SELECT_POST);
        List<Object> params = new ArrayList<Object>();
        String where = buildWhere(boardId, keyword, params);
        sb.append(where);
        sb.append(buildOrder(sort));
        sb.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(start);
        return query(sb.toString(), params.toArray(), POST_MAPPER);
    }

    /**
     * 统计符合条件的帖子数量
     *
     * @param boardId 板块 ID（0 表示全部）
     * @param keyword 关键词
     * @return 帖子总数
     */
    public int countList(int boardId, String keyword) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(1) FROM t_post p ");
        List<Object> params = new ArrayList<Object>();
        sb.append(buildWhere(boardId, keyword, params));
        return count(sb.toString(), params.toArray());
    }

    /**
     * 构建 WHERE 子句，同时把参数按顺序放入 params
     *
     * @param boardId 板块 ID
     * @param keyword 关键词（匹配标题或正文）
     * @param params  参数容器
     * @return WHERE 子句
     */
    private String buildWhere(int boardId, String keyword, List<Object> params) {
        StringBuilder sb = new StringBuilder(" WHERE 1=1 ");
        if (boardId > 0) {
            sb.append(" AND p.board_id=? ");
            params.add(boardId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append(" AND (p.title LIKE ? OR p.content LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        return sb.toString();
    }

    /**
     * 构建 ORDER BY 子句：置顶帖永远在最前
     *
     * @param sort new / hot
     * @return ORDER BY 子句
     */
    private String buildOrder(String sort) {
        if ("hot".equals(sort)) {
            return " ORDER BY p.pinned DESC, p.likes DESC, p.views DESC ";
        }
        return " ORDER BY p.pinned DESC, p.created_time DESC ";
    }

    /** 根据 ID 查询帖子详情（联表） */
    public Post findById(int id) {
        return queryOne(SELECT_POST + " WHERE p.id=?", new Object[]{id}, POST_MAPPER);
    }

    /** 新增帖子 */
    public int insert(Post post) {
        String sql = "INSERT INTO t_post(board_id,title,content,image_path,user_id,views,likes,pinned,created_time,updated_time) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";
        long now = System.currentTimeMillis();
        return update(sql, new Object[]{
                post.getBoardId(), post.getTitle(), post.getContent(), post.getImagePath(), post.getUserId(),
                0, 0, post.getPinned(), now, now
        });
    }

    /** 更新帖子的板块、标题、正文与配图（编辑用） */
    public int updateContent(int id, int boardId, String title, String content, String imagePath) {
        String sql = "UPDATE t_post SET board_id=?,title=?,content=?,image_path=?,updated_time=? WHERE id=?";
        return update(sql, new Object[]{boardId, title, content, imagePath, System.currentTimeMillis(), id});
    }

    /** 删除帖子（关联的回复和点赞由 Service 层负责清理） */
    public int delete(int id) {
        return update("DELETE FROM t_post WHERE id=?", new Object[]{id});
    }

    /** 浏览数 +1 */
    public int increaseViews(int id) {
        return update("UPDATE t_post SET views=views+1 WHERE id=?", new Object[]{id});
    }

    /** 置顶 / 取消置顶 */
    public int updatePinned(int id, int pinned) {
        return update("UPDATE t_post SET pinned=? WHERE id=?", new Object[]{pinned, id});
    }

    /** 删除某个帖子下的全部点赞记录（删帖时调用） */
    public int deleteLikesByPostId(int postId) {
        return update("DELETE FROM t_like WHERE post_id=?", new Object[]{postId});
    }

    /** 同步 likes 字段为点赞表里的真实数量 */
    public int syncLikes(int postId) {
        int n = count("SELECT COUNT(1) FROM t_like WHERE post_id=?", new Object[]{postId});
        update("UPDATE t_post SET likes=? WHERE id=?", new Object[]{n, postId});
        return n;
    }

    /** 查询是否点过赞 */
    public boolean hasLiked(int postId, int userId) {
        return count("SELECT COUNT(1) FROM t_like WHERE post_id=? AND user_id=?",
                new Object[]{postId, userId}) > 0;
    }

    /** 新增点赞记录 */
    public int addLike(int postId, int userId) {
        return update("INSERT INTO t_like(post_id,user_id,created_time) VALUES(?,?,?)",
                new Object[]{postId, userId, System.currentTimeMillis()});
    }

    /** 取消点赞 */
    public int removeLike(int postId, int userId) {
        return update("DELETE FROM t_like WHERE post_id=? AND user_id=?", new Object[]{postId, userId});
    }

    /** 热门帖 TopN（按点赞数、浏览数排序） */
    public List<Post> findHot(int limit) {
        return query(SELECT_POST + " ORDER BY p.likes DESC, p.views DESC LIMIT ?",
                new Object[]{limit}, POST_MAPPER);
    }

    /** 查询某个用户发的帖子（分页） */
    public List<Post> findByUser(int userId, int start, int size) {
        return query(SELECT_POST + " WHERE p.user_id=? ORDER BY p.created_time DESC LIMIT ? OFFSET ?",
                new Object[]{userId, size, start}, POST_MAPPER);
    }

    /** 查询某个板块下的全部帖子（删除板块时使用） */
    public List<Post> findByBoard(int boardId) {
        return query(SELECT_POST + " WHERE p.board_id=? ORDER BY p.created_time DESC",
                new Object[]{boardId}, POST_MAPPER);
    }

    /** 删除某个板块下的全部帖子（连同回复/点赞由 Service 负责） */
    public int deleteByBoardId(int boardId) {
        return update("DELETE FROM t_post WHERE board_id=?", new Object[]{boardId});
    }

    /** 统计某个时间戳之后的帖子数（用于「今日新帖」统计） */
    public int countSince(long ts) {
        return count("SELECT COUNT(1) FROM t_post WHERE created_time>=?", new Object[]{ts});
    }

    /** 某个用户发帖总数 */
    public int countByUser(int userId) {
        return count("SELECT COUNT(1) FROM t_post WHERE user_id=?", new Object[]{userId});
    }

    /** 站点统计：帖子总数、今日新帖 */
    public int count() {
        return count("SELECT COUNT(1) FROM t_post", null);
    }
}
