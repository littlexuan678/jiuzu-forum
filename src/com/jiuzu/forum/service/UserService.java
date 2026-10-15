package com.jiuzu.forum.service;

import com.jiuzu.forum.bean.User;
import com.jiuzu.forum.dao.UserDao;
import com.jiuzu.forum.util.Md5Util;

/**
 * 用户业务逻辑层
 *
 * <p>负责注册校验、密码加密、登录验证等业务流程，DAO 只做纯粹的数据存取。</p>
 *
 * @author 第九小组
 */
public class UserService {

    private UserDao userDao = new UserDao();

    /**
     * 用户注册
     *
     * @param username 用户名（4~16 位字母、数字、下划线）
     * @param password 明文密码
     * @param nickname 昵称
     * @return 注册后的用户对象
     * @throws IllegalArgumentException 参数不合法或用户名已被占用时抛出
     */
    public User register(String username, String password, String nickname) {
        if (username == null || !username.matches("^[A-Za-z0-9_]{4,16}$")) {
            throw new IllegalArgumentException("用户名需为 4~16 位字母、数字或下划线");
        }
        if (password == null || password.length() < 6 || password.length() > 20) {
            throw new IllegalArgumentException("密码长度需在 6~20 位之间");
        }
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("该用户名已经被注册了");
        }
        String displayName = (nickname == null || nickname.trim().isEmpty()) ? username : nickname.trim();
        // 从一组预设色里按用户名哈希取一个，保证同一个人颜色固定
        String[] colors = {"#0088f5", "#f56a79", "#2e9a1a", "#faad14", "#a855f7", "#06b6d4", "#f97316"};
        int idx = Math.abs(username.hashCode()) % colors.length;

        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.md5(password));
        user.setNickname(displayName);
        user.setAvatarColor(colors[idx]);
        user.setRole(User.ROLE_NORMAL);
        user.setBanned(0);
        userDao.insert(user);
        return userDao.findByUsername(username);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录成功返回用户对象；失败返回 null
     */
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            return null;
        }
        if (!Md5Util.md5(password).equals(user.getPassword())) {
            return null;
        }
        if (user.isBanned()) {
            throw new SecurityException("该账号已被管理员封禁");
        }
        userDao.updateLastLoginTime(user.getId());
        return user;
    }

    /**
     * 更新个人资料：昵称 + 头像
     *
     * @param userId   用户 ID
     * @param nickname 昵称（可中文，1~20 字）
     * @param avatar   头像图片地址，可为空表示使用文字头像
     */
    public void updateProfile(int userId, String nickname, String avatar) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (nickname.trim().length() > 20) {
            throw new IllegalArgumentException("昵称不能超过 20 个字");
        }
        String av = (avatar == null || avatar.trim().isEmpty()) ? null : avatar.trim();
        if (av != null && av.length() > 200) {
            throw new IllegalArgumentException("头像地址不合法");
        }
        userDao.updateProfile(userId, nickname.trim(), av);
    }

    /**
     * 修改密码：需校验原密码
     *
     * @param userId  用户 ID
     * @param oldPwd  原密码（明文）
     * @param newPwd  新密码（明文，6~20 位）
     * @param current 当前用户对象（含数据库中的密码）
     */
    public void changePassword(int userId, String oldPwd, String newPwd, User current) {
        if (oldPwd == null || !Md5Util.md5(oldPwd).equals(current.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (newPwd == null || newPwd.length() < 6 || newPwd.length() > 20) {
            throw new IllegalArgumentException("新密码长度需在 6~20 位之间");
        }
        if (oldPwd.equals(newPwd)) {
            throw new IllegalArgumentException("新密码不能与原密码相同");
        }
        userDao.updatePassword(userId, Md5Util.md5(newPwd));
    }

    /** 修改昵称 */
    public void updateNickname(int userId, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        userDao.updateNickname(userId, nickname.trim());
    }

    /** 查询单个用户 */
    public User getById(int id) {
        return userDao.findById(id);
    }
}
