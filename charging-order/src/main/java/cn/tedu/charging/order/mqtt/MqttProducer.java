package cn.tedu.charging.order.mqtt;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务需要什么,我们就给业务提供什么入口方法
 */
@Component
@Slf4j
public class MqttProducer {
    @Autowired
    private MqttClient mqttClient;
    //做一个底层执行生产发送的方法
    private void doSend(String topic,byte[] payload,int qos,boolean retained){
        try{
            // 检查MQTT客户端连接状态
            if (!mqttClient.isConnected()) {
                log.warn("MQTT客户端未连接，无法发送消息到主题: {}", topic);
                throw new RuntimeException("MQTT客户端未连接");
            }
            
            //1.组织一个消息对象
            MqttMessage message=new MqttMessage();
            message.setPayload(payload);
            message.setQos(qos);
            message.setRetained(retained);
            //2.发送消息
            mqttClient.publish(topic,message);
        }catch (Exception e){
            log.error("发送消息失败",e);
            // 如果连接断开，记录警告
            if (mqttClient != null && !mqttClient.isConnected()) {
                log.warn("MQTT客户端当前连接状态: {}", mqttClient.isConnected());
            }
        }
    }
    //给业务提供一个 默认参数的消息发送,让业务传递数据是object对象
    public void sendDefault(String topic,Object msg){
        //1.需要将msg做序列化 json 转化成 string 转化byte[]
        byte[] payLoad= JSON.toJSONString( msg).getBytes();
        //2.调用doSend qos=1 retained=true 默认
        doSend(topic,payLoad,1,true);
    }
    public void sendCustom(String topic,Object msg,int qos,boolean retained){
        //1.需要将msg做序列化 json 转化成 string 转化byte[]
        byte[] payLoad= JSON.toJSONString( msg).getBytes();
        //2.调用doSend qos=1 retained=true 默认
        doSend(topic,payLoad,qos,retained);
    }
}