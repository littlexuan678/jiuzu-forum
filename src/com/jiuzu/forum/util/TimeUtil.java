package com.jiuzu.forum.util;

/**
 * 时间格式化工具类
 *
 * <p>把时间戳转换成“刚刚 / 3 分钟前 / 昨天 14:20”这类友好显示，
 * 在列表页直接通过 EL 调用静态方法：
 * {@code ${tu:friendly(p.createdTime)}} 需要先注册函数，本项目统一在 JSP 中
 * 通过 {@code TimeUtil} 处理，简化为静态方法调用。</p>
 *
 * @author 第九小组
 */
public class TimeUtil {

    /**
     * 返回友好的时间描述
     *
     * @param timestamp 时间戳（毫秒）
     * @return 友好字符串
     */
    public static String friendly(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return "";
        }
        String result = friendly(timestamp.longValue());
        return result;
    }

    /**
     * 返回友好的时间描述
     *
     * @param timestamp 时间戳（毫秒）
     * @return 友好字符串
     */
    public static String friendly(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 0) {
            diff = 0;
        }
        long minute = 60 * 1000L;
        long hour = 60 * minute;
        long day = 24 * hour;

        if (diff < minute) {
            return "刚刚";
        } else if (diff < hour) {
            return (diff / minute) + " 分钟前";
        } else if (diff < day) {
            return (diff / hour) + " 小时前";
        } else if (diff < 2 * day) {
            return "昨天 " + formatTime(timestamp);
        } else if (diff < 30 * day) {
            return (diff / day) + " 天前";
        }
        return format(timestamp);
    }

    /** HH:mm */
    private static String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }

    /** yyyy-MM-dd */
    private static String format(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date(timestamp));
    }
}
