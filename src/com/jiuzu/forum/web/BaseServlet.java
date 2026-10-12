package com.jiuzu.forum.web;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.filter.AuthFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet 基类
 *
 * <p>抽取了控制器里反复用到的动作：获取登录用户、转发到 JSP、重定向、
 * 读取请求参数、设置错误提示等，让各个业务 Servlet 只关心自己的流程。</p>
 *
 * @author 第九小组
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** JSP 页面统一放在 /WEB-INF/jsp 下，避免被直接访问 */
    protected static final String JSP = "/WEB-INF/jsp/";

    /** 获取当前登录用户，未登录返回 null */
    protected User getLoginUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute(AuthFilter.SESSION_USER);
    }

    /** 要求必须登录，否则抛出异常由调用方处理 */
    protected User requireLogin(HttpServletRequest req) {
        User user = getLoginUser(req);
        if (user == null) {
            throw new IllegalStateException("请先登录");
        }
        return user;
    }

    /** 转发到指定的 JSP 页面 */
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        req.getRequestDispatcher(JSP + jsp).forward(req, resp);
    }

    /** 重定向（自动带上项目上下文路径） */
    protected void redirect(HttpServletResponse resp, HttpServletRequest req, String path)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** 读取 int 参数，缺省返回默认值 */
    protected int getIntParam(HttpServletRequest req, String name, int def) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /** 读取字符串参数，缺省返回空串 */
    protected String getStringParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    /** 设置错误提示（JSP 中用 ${error} 显示） */
    protected void setError(HttpServletRequest req, String msg) {
        req.setAttribute("error", msg);
    }

    /** 设置操作成功提示（JSP 中用 ${message} 显示） */
    protected void setMessage(HttpServletRequest req, String msg) {
        req.setAttribute("message", msg);
    }
}
