package cn.tedu.charging.order.mqtt;

import cn.tedu.charging.common.constant.MqttTopicConst;
import cn.tedu.charging.common.pojo.message.CheckResultMessage;
import cn.tedu.charging.common.pojo.message.ProgressMessage;
import cn.tedu.charging.order.service.ConsumerService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MqttClientCallback implements MqttCallbackExtended {
    /**
     * connectComplete 在连接对象 connect执行 且成功建立连接的时候调用
     * @param reconnect 当前连接是否是断开重连 true是,false否 第一次连接
     * @param serverURI 连接emqx的address tcp://192.168.8.100:1883
     */
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect){
            log.info("重新连接成功,serverURI:{}",serverURI);
        }else{
            log.info("首次连接成功,serverURI:{}",serverURI);
        }
    }
    /**
     * connectionLost 在连接对象 connect执行 且连接断开的时候调用
     * @param cause 连接断开的原因 异常原因
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.info("连接断开,异常原因:{}",cause.getMessage());
    }
    /**
     * messageArrived 在连接对象订阅主题后,且主题中有可以消费的消息时,消息会投递给客户端,当前方法会被调用
     * @param topic 消息来源的主题
     * @param message 消息具体数据对象
     * 这个方法是消费的入口方法
     */
    @Autowired
    private ConsumerService consumerService;
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info("收到消息,topic:{} message:{}",topic,message.toString());
        //因为消息来源可能是不同主题,处理的业务逻辑有所区别,所以要判断一下
        if (topic.equals(MqttTopicConst.GUN_CHECK_RESULT_TOPIC)){
            log.debug("当前消息来自设备自检主题,处理自检反馈逻辑");
            //把message转化成CheckResultMessage对象
            CheckResultMessage msg = JSON.parseObject(message.toString(), CheckResultMessage.class);
            //调用业务层处理消费逻辑
            consumerService.handleCheckResult(msg);
        }else if(topic.equals(MqttTopicConst.CHARGING_PROGRESS_TOPIC)){
            log.debug("当前消息来自设备充电进度同步信息");
            //消息json转化成对象
            ProgressMessage msg=JSON.parseObject(message.toString(),ProgressMessage.class);
            //调用业务处理消息
            consumerService.handleChargingProgress(msg);
        }
    }
    /**
     * deliveryComplete 在客户端对象发布消息后 被调用,可以确认消息发送的结果 成功|失败
     * @param token 确认消息对象 如果消息发送成功(服务反馈成功) 或者失败(服务端反馈失败) 都可以从token知道
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        boolean complete = token.isComplete();
        if (complete){
            log.info("消息发送成功");
        }else{
            log.info("消息发送失败");
        }

    }
}
