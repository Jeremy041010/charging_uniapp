package cn.tedu.charging.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;

@Slf4j
public class CronUtil {
    //根据传递的时间 计算返回一个可以调用定时任务的cron表达式

    public static String delayCron(int delayTime){
        // 计算1分钟后的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, delayTime);  // 增加1分钟
        Date cronTime = calendar.getTime();
        // 转换为CRON表达式（7位格式，包含秒和年）
        String cronExpression = String.format("%d %d %d %d %d ? %d",
                calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,  // 月份需要+1
                calendar.get(Calendar.YEAR)
        );
        log.debug("延迟时间:{}秒,cron表达式:{}",delayTime/1000,cronExpression);
        return cronExpression;
    }
}
