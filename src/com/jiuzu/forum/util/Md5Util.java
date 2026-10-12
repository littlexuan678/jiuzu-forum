package com.jiuzu.forum.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 *
 * <p>出于演示目的，对用户密码做 MD5 加盐摘要后再入库，
 * 避免数据库中出现明文密码。</p>
 *
 * @author 第九小组
 */
public class Md5Util {

    /** 盐值：演示用，实际项目应使用每个用户独立的随机盐 */
    private static final String SALT = "{jiuzu-forum}";

    /**
     * 计算字符串的 MD5 值（32 位小写）
     *
     * @param source 原文
     * @return MD5 摘要字符串
     */
    public static String md5(String source) {
        if (source == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest((source + SALT).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持 MD5 算法", e);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
}
