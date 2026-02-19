package cn.tedu.charging.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class DeclareConfiguration {
    private static final String DELAY_EX="DELAY_EX";
    private static final String DELAY_Q="DELAY_Q";
    private static final String DLX_EX="DLX_EX";
    private static final String DLX_Q="DLX_Q";
    private static final String DLX_RK="DLX_RK";
    //声明延迟交换机
    @Bean
    public Exchange delayEx(){
        //直接返回一个实例化对象
        return new FanoutExchange(DELAY_EX);
    }
    //声明延迟队列,绑定死信交换机和死信路由
    @Bean
    public Queue delayQueue(){
        Map<String,Object> args=new HashMap<>();
        args.put("x-dead-letter-exchange",DLX_EX);
        args.put("x-dead-letter-routing-key",DLX_RK);
        return new Queue(DELAY_Q,false,false,false,args);
    }
    //绑定延迟队列到延迟交换机
    @Bean
    public Binding delayBinding(){
        //底层代码binding有两个方法 queueBind(队列绑定给交换机) exchangeBind(交换机绑定给交换机)
        return new Binding(DELAY_Q,Binding.DestinationType.QUEUE,DELAY_EX,"",null);
    }
    //声明死信交换机 类型direct
    @Bean
    public Exchange dlxEx(){
        return new DirectExchange(DLX_EX);
    }
    //死信队列
    @Bean
    public Queue dlxQueue(){
        return new Queue(DLX_Q,false,false,false,null);
    }
    //使用死信路由key绑定
    @Bean
    public Binding dlxBinding(){
        return new Binding(DLX_Q,Binding.DestinationType.QUEUE,DLX_EX,DLX_RK,null);
    }
}
