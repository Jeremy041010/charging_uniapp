package cn.tedu.charging.order.timer;

import cn.tedu.charging.order.service.OrderService;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 作为定时任务的程序入口类
 */
@Component
@Slf4j
public class OrderCheckTimer {
    @Autowired
    private OrderService orderService;
    //检查某张订单的 实际状态
    @XxlJob("order-status-check")
    public void checkOrderStatus(){
        String billId = XxlJobContext.getXxlJobContext().getJobParam();
        log.info("检查订单状态,billId:{}",billId);
        orderService.checkOrderStatus(billId);
    }
}