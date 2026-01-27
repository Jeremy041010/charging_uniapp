package cn.tedu.charging.order.amqp;

import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AmqpDelayProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    //业务发送的是普通消息,消息内容可能是字符串也可能是object对象
    public void sendDefault(String destination,Object msg){
        this.doSend(destination,null,msg,null);
    }

    //业务发送的是延迟消息,业务确定延迟时间,消息可能发送的是字符串,也可能是object对象
    public void sendDelay(String destination,Object msg,Integer delay){
        this.doSend(destination,null,msg,delay);
    }
    private void doSend(String exchange,String routingKey,Object msg,Integer delay){
        //1.考虑objectmsg的序列化问题
        byte[] body=null;
        if (msg instanceof String){
            log.info("发送普通字符串消息:"+msg);
            body=((String)msg).getBytes(StandardCharsets.UTF_8);
        }else{
            log.info("发送普通对象消息:"+msg);
            //使用json
            body= JSON.toJSONString(msg).getBytes(StandardCharsets.UTF_8);
        }
        MessageProperties properties=new MessageProperties();
        properties.setContentEncoding("utf-8");
        //设置延迟时间
        if (delay!=null&&delay>0){
            log.info("说明设置了延迟时间:{}",delay);
            properties.setExpiration(delay.toString());
        }
        Message message=new Message(body,properties);
        rabbitTemplate.send(exchange,routingKey,message);
    }


}