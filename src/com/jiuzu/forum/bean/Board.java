package com.jiuzu.forum.bean;

import java.io.Serializable;

/**
 * 板块实体类（JavaBean）
 *
 * <p>对应数据库表 t_board。</p>
 *
 * @author 第九小组
 */
public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    /** 板块名称 */
    private String name;
    /** 板块图标（emoji / 字符） */
    private String icon;
    /** 板块简介 */
    private String description;
    /** 排序号，越大越靠前 */
    private Integer sortOrder;
    /** 该板块下的帖子数（统计字段，非数据库列） */
    private Integer postCount;

    public Board() {
    }

    public Board(String name, String icon, String description, Integer sortOrder) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }
}
