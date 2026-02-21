package cn.tedu.charging.order.amqp;

import cn.tedu.charging.common.pojo.message.DelayCheckMessage;
import cn.tedu.charging.order.pojo.po.ChargingBillEndPO;
import cn.tedu.charging.order.service.ConsumerService;
import cn.tedu.charging.order.service.OrderService;
import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AmqpDelayConsumer {
    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private OrderService orderService;
    //监听新的队列 死信队列获取延迟消息
    @RabbitListener(queues="DLX_Q")
    public void delayConsume(Message message, Channel channel){
        log.info("consumer获取延迟消息:{}",new String(message.getBody(),StandardCharsets.UTF_8));
        
        try {
            String msgJson = new String(message.getBody(), StandardCharsets.UTF_8);
            
            // 判断消息类型并分别处理
            if (msgJson.contains("orderNo") && msgJson.contains("userId") && msgJson.contains("gunId")) {
                // 设备无响应检查消息
                DelayCheckMessage msg = JSON.parseObject(msgJson, DelayCheckMessage.class);
                consumerService.handleCheckNoRes(msg);
                log.debug("处理设备无响应检查消息完成, orderNo:{}", msg.getOrderNo());
            } else if (msgJson.contains("billId") && msgJson.contains("consumeAmount")) {
                // 订单结束消息
                ChargingBillEndPO endMsg = JSON.parseObject(msgJson, ChargingBillEndPO.class);
                // 通过OrderService处理订单结束逻辑
                orderService.endOrder(endMsg.getBillId(), endMsg);
                log.info("异步处理订单结束完成, billId:{}", endMsg.getBillId());
            } else {
                log.warn("无法识别的延迟消息格式: {}", msgJson);
            }
            
            // 确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            
        } catch (Exception e) {
            log.error("处理延迟消息失败", e);
            try {
                // 拒绝消息并重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception nackEx) {
                log.error("拒绝消息失败", nackEx);
            }
        }
    }
}