package com.jiuzu.forum.dao;

import com.jiuzu.forum.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO 基础类
 *
 * <p>对 JDBC 的重复代码做了二次封装：获取连接、预编译 SQL、绑定参数、
 * 执行查询/更新、关闭资源。子类只需关注 SQL 与结果映射。</p>
 *
 * @author 第九小组
 */
public class BaseDao {

    /**
     * 绑定 PreparedStatement 参数
     *
     * @param ps    预编译语句
     * @param params 参数列表
     */
    private void bindParams(PreparedStatement ps, Object[] params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /**
     * 通用查询，返回实体列表
     *
     * @param sql     SQL 语句（可含 ? 占位符）
     * @param params  参数
     * @param mapper  行映射器
     * @param <T>     实体类型
     * @return 实体列表（不会为 null）
     */
    public <T> List<T> query(String sql, Object[] params, RowMapper<T> mapper) {
        List<T> list = new ArrayList<T>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            bindParams(ps, params);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 查询单条记录
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @param mapper 行映射器
     * @param <T>    实体类型
     * @return 实体对象，不存在时返回 null
     */
    public <T> T queryOne(String sql, Object[] params, RowMapper<T> mapper) {
        List<T> list = query(sql, params, mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 执行增删改
     *
     * @param sql    SQL 语句
     * @param params 参数
     * @return 影响的行数
     */
    public int update(String sql, Object[] params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            bindParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(ps);
            DBUtil.close(conn);
        }
    }

    /**
     * 统计记录数，常用于分页
     *
     * @param sql    select count(1) ...
     * @param params 参数
     * @return 记录条数
     */
    public int count(String sql, Object[] params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            bindParams(ps, params);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("统计失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }
}
