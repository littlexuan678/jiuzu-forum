package com.jiuzu.forum.dao;

import com.jiuzu.forum.bean.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 用户表（t_user）数据访问对象
 *
 * <p>封装了用户相关的全部 JDBC 操作：单表查询、新增、更新、删除，
 * 对应考核要求的“用 JDBC 完成数据库连接，并实现单表的查询及数据管理操作”。</p>
 *
 * @author 第九小组
 */
public class UserDao extends BaseDao {

    /** 用户行映射器 */
    private static final RowMapper<User> USER_MAPPER = new RowMapper<User>() {
        public User mapRow(ResultSet rs) throws SQLException {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setPassword(rs.getString("password"));
            u.setNickname(rs.getString("nickname"));
            u.setAvatarColor(rs.getString("avatar_color"));
            // 头像图片地址（个人中心上传，可为空，为空时显示文字头像）
            try {
                u.setAvatar(rs.getString("avatar"));
            } catch (SQLException ignore) {
                // 老库还没加 avatar 列时忽略，迁移完成后不会再走到这里
            }
            u.setRole(rs.getInt("role"));
            u.setBanned(rs.getInt("banned"));
            u.setCreatedTime(rs.getLong("created_time"));
            u.setLastLoginTime(rs.getLong("last_login_time"));
            return u;
        }
    };

    /** 根据用户名查询（用于登录） */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM t_user WHERE username=?";
        return queryOne(sql, new Object[]{username}, USER_MAPPER);
    }

    /** 根据 ID 查询 */
    public User findById(int id) {
        String sql = "SELECT * FROM t_user WHERE id=?";
        return queryOne(sql, new Object[]{id}, USER_MAPPER);
    }

    /** 新增用户，返回新用户 ID */
    public int insert(User user) {
        String sql = "INSERT INTO t_user(username,password,nickname,avatar_color,role,banned,created_time) " +
                "VALUES(?,?,?,?,?,?,?)";
        return update(sql, new Object[]{
                user.getUsername(), user.getPassword(), user.getNickname(),
                user.getAvatarColor(), user.getRole(), user.getBanned(),
                System.currentTimeMillis()
        });
    }

    /** 更新最后登录时间 */
    public int updateLastLoginTime(int id) {
        return update("UPDATE t_user SET last_login_time=? WHERE id=?",
                new Object[]{System.currentTimeMillis(), id});
    }

    /** 更新封禁状态：1 封禁，0 解封 */
    public int updateBanned(int id, int banned) {
        return update("UPDATE t_user SET banned=? WHERE id=?", new Object[]{banned, id});
    }

    /** 修改用户角色 */
    public int updateRole(int id, int role) {
        return update("UPDATE t_user SET role=? WHERE id=?", new Object[]{role, id});
    }

    /** 修改昵称 */
    public int updateNickname(int id, String nickname) {
        return update("UPDATE t_user SET nickname=? WHERE id=?", new Object[]{nickname, id});
    }

    /** 修改个人资料：昵称 + 头像地址 */
    public int updateProfile(int id, String nickname, String avatar) {
        return update("UPDATE t_user SET nickname=?, avatar=? WHERE id=?",
                new Object[]{nickname, avatar, id});
    }

    /** 修改密码（传入的已是 MD5 后的密文） */
    public int updatePassword(int id, String md5Password) {
        return update("UPDATE t_user SET password=? WHERE id=?", new Object[]{md5Password, id});
    }

    /** 删除用户（同时清理其关联数据由 Service 层负责） */
    public int delete(int id) {
        return update("DELETE FROM t_user WHERE id=?", new Object[]{id});
    }

    /** 用户总数 */
    public int count() {
        return count("SELECT COUNT(1) FROM t_user", null);
    }

    /** 分页查询用户列表 */
    public List<User> findByPage(int start, int size) {
        String sql = "SELECT * FROM t_user ORDER BY id DESC LIMIT ? OFFSET ?";
        return query(sql, new Object[]{size, start}, USER_MAPPER);
    }
}
