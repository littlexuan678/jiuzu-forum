package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.service.ReplyService;
import com.jiuzu.forum.util.IpUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 发表回复控制器
 *
 * @author 第九小组
 */
public class ReplyAddServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private ReplyService replyService = new ReplyService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = requireLogin(req);
        boolean mp = isMultipart(req);
        int postId = parseInt(mp ? getMultipartString(req, "postId") : getStringParam(req, "postId"), 0);
        String content = mp ? getMultipartString(req, "content") : getStringParam(req, "content");
        // 楼中楼：parentId>0 表示回复某一条回复（被回复人昵称由服务端从库中取，不接受前端传入）
        int parentId = parseInt(mp ? getMultipartString(req, "parentId") : getStringParam(req, "parentId"), 0);

        String imagePath = null;
        if (mp) {
            try {
                imagePath = saveMultipartImage(req, "image", "replies");
            } catch (IllegalArgumentException e) {
                req.getSession().setAttribute("flashError", e.getMessage());
                redirect(resp, req, "/post/detail?id=" + postId);
                return;
            }
        }

        // 记录回复 IP 与属地（前台只展示属地，完整 IP 供后台查看）
        String ip = IpUtil.getClientIp(req);
        String ipLoc = IpUtil.getLocation(ip);

        try {
            replyService.create(postId, content, user, imagePath, ip, ipLoc, parentId);
        } catch (IllegalArgumentException | SecurityException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        redirect(resp, req, "/post/detail?id=" + postId);
    }
}
