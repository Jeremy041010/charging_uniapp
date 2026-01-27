package cn.tedu.charging.order.amqp;

import cn.tedu.charging.common.pojo.message.DelayCheckMessage;
import cn.tedu.charging.order.service.ConsumerService;
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
    //监听新的队列 死信队列获取延迟消息
    @RabbitListener(queues="DLX_Q")
    public void delayConsume(Message message, Channel channel){
        log.info("consumer获取延迟消息:{}",new String(message.getBody(),StandardCharsets.UTF_8));
        //1.解析消息数据 转化会业务数据DelayCheckMessage
        String msgJson=new String(message.getBody(),StandardCharsets.UTF_8);
        DelayCheckMessage msg= JSON.parseObject(msgJson,DelayCheckMessage.class);
        //2. 消费延迟业务
        try{
            consumerService.handleCheckNoRes(msg);
        }catch (Exception e){
            log.error("处理延迟消息失败",e);
        }
        //执行确认
        try{
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            log.error("确认消息失败",e);
        }
    }
}