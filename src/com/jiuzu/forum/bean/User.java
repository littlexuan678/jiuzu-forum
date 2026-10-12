package com.jiuzu.forum.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类（JavaBean）
 *
 * <p>封装用户表（t_user）的一条记录，符合 JavaBean 规范：
 * 私有属性 + 无参构造 + getter/setter，便于在 JSP 中通过 EL 表达式访问。</p>
 *
 * @author 第九小组
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色：普通用户 */
    public static final int ROLE_NORMAL = 0;
    /** 角色：管理员 */
    public static final int ROLE_ADMIN = 1;

    private Integer id;
    private String username;
    private String password;
    private String nickname;
    /** 头像底色，用于渲染纯色头像 */
    private String avatarColor;
    /** 角色：0 普通用户，1 管理员 */
    private Integer role;
    /** 是否封禁：0 正常，1 已封禁 */
    private Integer banned;
    /** 注册时间（时间戳，毫秒） */
    private Long createdTime;
    /** 最后登录时间（时间戳，毫秒） */
    private Long lastLoginTime;

    public User() {
    }

    // ---------- 供 JSP 使用的辅助属性（EL 表达式会调用对应的 getter） ----------

    /** 是否为管理员，JSP 中 ${sessionScope.user.admin} */
    public boolean isAdmin() {
        return this.role != null && this.role.intValue() == ROLE_ADMIN;
    }

    /** 是否被封禁 */
    public boolean isBanned() {
        return this.banned != null && this.banned.intValue() == 1;
    }

    /** 头像文字：昵称或用户名的第一个字 */
    public String getAvatarText() {
        String name = (nickname == null || nickname.isEmpty()) ? username : nickname;
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return name.substring(0, 1);
    }

    /** 注册时间转成 Date，供 JSTL fmt:formatDate 使用 */
    public Date getCreatedDate() {
        return createdTime == null ? null : new Date(createdTime);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getBanned() {
        return banned;
    }

    public void setBanned(Integer banned) {
        this.banned = banned;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
