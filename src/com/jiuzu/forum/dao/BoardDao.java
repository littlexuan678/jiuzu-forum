package com.jiuzu.forum.dao;

import com.jiuzu.forum.bean.Board;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 板块表（t_board）数据访问对象
 *
 * @author 第九小组
 */
public class BoardDao extends BaseDao {

    /** 板块行映射器（含每个板块的帖子数） */
    private static final RowMapper<Board> BOARD_MAPPER = new RowMapper<Board>() {
        public Board mapRow(ResultSet rs) throws SQLException {
            Board b = new Board();
            b.setId(rs.getInt("id"));
            b.setName(rs.getString("name"));
            b.setIcon(rs.getString("icon"));
            b.setDescription(rs.getString("description"));
            b.setSortOrder(rs.getInt("sort_order"));
            b.setPostCount(rs.getInt("post_count"));
            return b;
        }
    };

    /**
     * 查询全部板块，并统计各板块下的帖子数
     *
     * @return 板块列表（按 sort_order 倒序）
     */
    public List<Board> findAllWithCount() {
        String sql = "SELECT b.*, (SELECT COUNT(1) FROM t_post p WHERE p.board_id=b.id) AS post_count " +
                "FROM t_board b ORDER BY b.sort_order DESC, b.id ASC";
        return query(sql, null, BOARD_MAPPER);
    }

    /** 根据 ID 查询板块 */
    public Board findById(int id) {
        String sql = "SELECT b.*, (SELECT COUNT(1) FROM t_post p WHERE p.board_id=b.id) AS post_count " +
                "FROM t_board b WHERE b.id=?";
        return queryOne(sql, new Object[]{id}, BOARD_MAPPER);
    }

    /** 根据名称查询板块 */
    public Board findByName(String name) {
        String sql = "SELECT b.*, (SELECT COUNT(1) FROM t_post p WHERE p.board_id=b.id) AS post_count " +
                "FROM t_board b WHERE b.name=?";
        return queryOne(sql, new Object[]{name}, BOARD_MAPPER);
    }

    /** 新增板块 */
    public int insert(Board board) {
        String sql = "INSERT INTO t_board(name,icon,description,sort_order) VALUES(?,?,?,?)";
        return update(sql, new Object[]{board.getName(), board.getIcon(),
                board.getDescription(), board.getSortOrder()});
    }

    /** 删除板块 */
    public int delete(int id) {
        return update("DELETE FROM t_board WHERE id=?", new Object[]{id});
    }

    /** 更新板块信息 */
    public int update(Board board) {
        String sql = "UPDATE t_board SET name=?, icon=?, description=?, sort_order=? WHERE id=?";
        return update(sql, new Object[]{board.getName(), board.getIcon(),
                board.getDescription(), board.getSortOrder(), board.getId()});
    }

    /** 统计某板块下的帖子数 */
    public int countPosts(int boardId) {
        return count("SELECT COUNT(1) FROM t_post WHERE board_id=?", new Object[]{boardId});
    }

    /** 把某板块下的帖子整体迁移到另一个板块（删除板块前调用） */
    public int movePosts(int fromBoardId, int toBoardId) {
        return update("UPDATE t_post SET board_id=? WHERE board_id=?",
                new Object[]{toBoardId, fromBoardId});
    }
}
