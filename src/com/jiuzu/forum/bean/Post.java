package com.jiuzu.forum.bean;

import com.jiuzu.forum.util.IpUtil;

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
    /** 发布时的客户端 IP（仅后台可见，前台不展示） */
    private String ip;
    /** IP 属地文案（如「广东省广州市 电信」「本机」），前台展示用 */
    private String ipLoc;
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
    /** 帖子附件地址（如 /uploads/files/xxx.zip），无附件则为 null */
    private String attachmentUrl;
    /** 附件原始文件名，用于展示与下载时的文件名 */
    private String attachmentName;
    /** 附件大小的可读文案，如「1.2 MB」 */
    private String attachmentSize;

    public Post() {
    }

    /** 是否有附件（JSP 中 ${post.hasAttachment}） */
    public boolean isHasAttachment() {
        return attachmentUrl != null && !attachmentUrl.trim().isEmpty();
    }

    /**
     * 附件下载链接：在地址后带上原始文件名，
     * 这样 FileServlet 能以「实验报告.pdf」而不是随机名下载。
     */
    public String getAttachmentDownload() {
        if (!isHasAttachment()) {
            return null;
        }
        String url = getAttachmentUrl();
        if (attachmentName == null || attachmentName.trim().isEmpty()) {
            return url;
        }
        try {
            return url + "?name="
                    + java.net.URLEncoder.encode(attachmentName, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 统一成不带上下文前缀的相对地址（如 /uploads/files/xxx.zip）。
     *
     * <p>上传接口返回的是可直接用的绝对地址（含 /jiuzu-forum），而页面渲染时会再拼一次
     * ${ctx}，直接入库会导致 /jiuzu-forum/jiuzu-forum/uploads/... 这种坏链。这里统一裁掉。</p>
     */
    private static String stripContext(String url) {
        if (url == null) {
            return null;
        }
        String u = url.trim();
        if (u.startsWith("/uploads/")) {
            return u;
        }
        int i = u.indexOf("/uploads/");
        return i > 0 ? u.substring(i) : u;
    }

    /** 附件类型图标：压缩包 / 文档 / 表格 / 幻灯片 / 文本 / 默认 */
    public String getAttachmentIcon() {
        String n = attachmentName == null ? "" : attachmentName.toLowerCase();
        if (n.endsWith(".zip") || n.endsWith(".rar") || n.endsWith(".7z") || n.endsWith(".tar") || n.endsWith(".gz")) {
            return "🗜️";
        }
        if (n.endsWith(".pdf")) {
            return "📕";
        }
        if (n.endsWith(".doc") || n.endsWith(".docx")) {
            return "📘";
        }
        if (n.endsWith(".xls") || n.endsWith(".xlsx") || n.endsWith(".csv")) {
            return "📗";
        }
        if (n.endsWith(".ppt") || n.endsWith(".pptx")) {
            return "📙";
        }
        if (n.endsWith(".txt") || n.endsWith(".md") || n.endsWith(".log")) {
            return "📄";
        }
        return "📦";
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

    public String getAttachmentUrl() {
        return stripContext(attachmentUrl);
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = stripContext(attachmentUrl);
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(String attachmentSize) {
        this.attachmentSize = attachmentSize;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIpLoc() {
        return ipLoc;
    }

    public void setIpLoc(String ipLoc) {
        this.ipLoc = ipLoc;
    }

    /**
     * 前台展示的 IP 属地：库里有属地就用属地，否则用 IP 现算一次并打码
     * （老数据没有属地列时也能正常显示）
     */
    public String getIpDisplay() {
        if (ipLoc != null && !ipLoc.trim().isEmpty()) {
            return ipLoc;
        }
        // 老数据没有记录 IP，直接返回 null，页面不展示该徽章
        if (ip == null || ip.trim().isEmpty()) {
            return null;
        }
        return IpUtil.getLocation(ip);
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
