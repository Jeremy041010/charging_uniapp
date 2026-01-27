package cn.tedu.charging.common.utils;

public class TimeConverterUtil {
    /**
     * 从总毫秒数中获取小时部分
     * @param totalTime 总毫秒数
     * @return 小时数
     */
    public static Long getHour(long totalTime) {
        return totalTime / (1000 * 60 * 60);
    }

    /**
     * 从总毫秒数中获取分钟部分（扣除小时后剩余的分钟）
     * @param totalTime 总毫秒数
     * @return 分钟数
     */
    public static Long getMinute(long totalTime) {
        return (totalTime % (1000 * 60 * 60)) / (1000 * 60);
    }

    /**
     * 从总毫秒数中获取秒部分（扣除小时和分钟后剩余的秒）
     * @param totalTime 总毫秒数
     * @return 秒数
     */
    public static Long getSecond(long totalTime) {
        return (totalTime % (1000 * 60)) / 1000;
    }

    /**
     * 获取剩余的毫秒数（可选方法）
     * @param totalTime 总毫秒数
     * @return 剩余的毫秒数
     */
    public static long getMillisecond(long totalTime) {
        return totalTime % 1000;
    }
}
