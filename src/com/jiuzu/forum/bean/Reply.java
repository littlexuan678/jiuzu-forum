package com.jiuzu.forum.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 回复实体类（JavaBean）
 *
 * <p>对应数据库表 t_reply。</p>
 *
 * @author 第九小组
 */
public class Reply implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    /** 所属帖子 ID */
    private Integer postId;
    /** 回复者 ID */
    private Integer userId;
    /** 回复者昵称（联表） */
    private String nickname;
    /** 回复者头像色（联表） */
    private String avatarColor;
    /** 回复内容 */
    private String content;
    /** 回复时间（时间戳，毫秒） */
    private Long createdTime;

    public Reply() {
    }

    public Date getCreatedDate() {
        return createdTime == null ? null : new Date(createdTime);
    }

    /** 友好时间描述，JSP 中用 ${reply.createdText} */
    public String getCreatedText() {
        return com.jiuzu.forum.util.TimeUtil.friendly(createdTime);
    }

    public String getAvatarText() {
        if (nickname == null || nickname.isEmpty()) {
            return "?";
        }
        return nickname.substring(0, 1);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
