package com.jiuzu.forum.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子实体类（JavaBean）
 *
 * <p>对应数据库表 t_post。其中 boardName、username 等字段来自联表查询，
 * 属于“业务视图属性”，方便 JSP 直接通过 EL 取值，避免页面再查库。</p>
 *
 * @author 第九小组
 */
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    /** 所属板块 ID */
    private Integer boardId;
    /** 所属板块名称（联表） */
    private String boardName;
    /** 板块图标（联表） */
    private String boardIcon;
    /** 标题 */
    private String title;
    /** 正文 */
    private String content;
    /** 帖子附带的封面 / 配图相对路径（如 /uploads/posts/xxx.jpg），无图则为 null */
    private String imagePath;
    /** 作者 ID */
    private Integer userId;
    /** 作者昵称（联表） */
    private String nickname;
    /** 作者头像色（联表） */
    private String avatarColor;
    /** 浏览量 */
    private Integer views;
    /** 点赞数 */
    private Integer likes;
    /** 回复数（统计字段） */
    private Integer replies;
    /** 是否置顶：0 否，1 是 */
    private Integer pinned;
    /** 发布时间（时间戳，毫秒） */
    private Long createdTime;
    /** 最后更新时间（时间戳，毫秒） */
    private Long updatedTime;
    /** 当前登录用户是否已点赞（非数据库列） */
    private boolean liked;

    public Post() {
    }

    /** 发布时间转 Date，供 fmt:formatDate 使用 */
    public Date getCreatedDate() {
        return createdTime == null ? null : new Date(createdTime);
    }

    /** 友好时间描述，如“刚刚 / 3 小时前”，JSP 中直接用 ${post.createdText} */
    public String getCreatedText() {
        return com.jiuzu.forum.util.TimeUtil.friendly(createdTime);
    }

    /** 正文摘要：去掉 Markdown 标记与换行后截取前 100 个字符 */
    public String getSummary() {
        if (content == null) {
            return "";
        }
        String text = com.jiuzu.forum.util.MarkupUtil.plain(content);
        return text.length() > 100 ? text.substring(0, 100) + "…" : text;
    }

    /**
     * 正文渲染结果（已做 HTML 转义，可直接在页面输出）
     *
     * <p>支持 ![]() 图片、**加粗**、`代码`、```代码块```、&gt; 引用、--- 分割线。</p>
     */
    public String getContentHtml() {
        return com.jiuzu.forum.util.MarkupUtil.render(content);
    }

    /** 是否为置顶帖 */
    public boolean isTop() {
        return pinned != null && pinned.intValue() == 1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBoardId() {
        return boardId;
    }

    public void setBoardId(Integer boardId) {
        this.boardId = boardId;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getBoardIcon() {
        return boardIcon;
    }

    public void setBoardIcon(String boardIcon) {
        this.boardIcon = boardIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getReplies() {
        return replies;
    }

    public void setReplies(Integer replies) {
        this.replies = replies;
    }

    public Integer getPinned() {
        return pinned;
    }

    public void setPinned(Integer pinned) {
        this.pinned = pinned;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
