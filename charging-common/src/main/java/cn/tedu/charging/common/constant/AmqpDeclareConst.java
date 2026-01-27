package cn.tedu.charging.common.constant;

/**
 * 声明rabbitmq组件的常量
 */
public class AmqpDeclareConst {
    /**
     * DELAY_BILL_EX
     * DELAY_BILL_QUEUE,
     * DELAY_BILL_DL_EX
     * DELAY_BILL_DL_QUEUE
     * DELAY_BILL_DL_RK
     */
    //业务交换机 消息到达的交换机
    public static final String DELAY_BILL_EX = "delay_bill_ex";
    //业务队列 消息最多存活1分钟
    public static final String DELAY_BILL_QUEUE = "delay_bill_queue";
    //死信交换机
    public static final String DELAY_BILL_DL_EX = "delay_bill_dl_ex";
    //死信队列
    public static final String DELAY_BILL_DL_QUEUE = "delay_bill_dl_queue";
    //死信路由
    public static final String DELAY_BILL_DL_RK = "delay_bill_dl_rk";
}
