package cn.tedu.test.charging.cron;

import cn.tedu.charging.common.utils.CronUtil;
import cn.tedu.charging.common.utils.XxlJobTaskUtil;

public class CronTest {
    public static void main(String[] args) {
        //直接使用cronUtil 工具类生成cron表达式
        String cronExpression = CronUtil.delayCron(1000 * 60 * 60 * 18);
        System.out.println(cronExpression);
        //XxlJobGroup jobGroupByName = XxlJobTaskUtil.getJobGroupByName("order-executor");
        //System.out.println(jobGroupByName.getId());
        XxlJobTaskUtil.createJobTask(cronExpression,"order-executor","123");
    }
}
