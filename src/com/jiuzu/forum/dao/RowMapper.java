package com.jiuzu.forum.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集映射接口
 *
 * <p>把 JDBC 的 {@link ResultSet} 一行记录转换成一个 JavaBean，
 * 封装了 DAO 层的通用查询逻辑，避免每个查询都重复写映射代码。</p>
 *
 * @param <T> 映射后的实体类型
 * @author 第九小组
 */
public interface RowMapper<T> {

    /**
     * 将当前行转换为实体对象
     *
     * @param rs 结果集（游标已指向某一行）
     * @return 实体对象
     * @throws SQLException SQL 异常
     */
    T mapRow(ResultSet rs) throws SQLException;
}
