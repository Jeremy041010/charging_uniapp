package cn.tedu.charging.order.config;

import cn.tedu.charging.common.constant.MqttTopicConst;
import cn.tedu.charging.order.mqtt.MqttClientCallback;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * 负责读取yaml的自定义属性 赋值给一个连接客户端
 * 容器管理这个客户端对象,随时注入到业务中使用,从而完成业务需要的生产和消费
 */
@Configuration
@Slf4j
public class MqttClientConfiguration {
    @Value("${charging.emqx.address}")
    private String address;
    @Value("${charging.emqx.username}")
    private String username;
    @Value("${charging.emqx.password}")
    private String password;
    @Autowired
    private MqttClientCallback callback;

    @Bean
    public MqttClient mqttClient() {
        log.info("开始创建MqttClient对象...");
        MqttClient mqttClient = null;
        //对这个链接对象做实例化,需要提供一些连接的参数数据比如 ip port id username password等
        try {
            //1.实例化 通过构造方法传递3个参数 address(消息代理地址) id(每个客户端id值) 内存文件存储
            mqttClient = new MqttClient(address, UUID.randomUUID().toString(), new MemoryPersistence());
            //2.同时 需要给这个客户端提供操作读写数据的权限 生产消息 消费消息
            MqttConnectOptions options = new MqttConnectOptions();
            //设置用户名密码
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            
            // 设置连接超时时间和心跳间隔
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);
            
            // 设置自动重连
            options.setAutomaticReconnect(true);
            
            // 设置CleanSession为true，确保每次连接都是新会话
            options.setCleanSession(true);
            
            // 给客户端对象 设置绑定一个回调函数
            mqttClient.setCallback(callback);
            
            //3.将选项配置 交给客户端,创建建立连接
            mqttClient.connect(options);
            log.info("MqttClient连接成功");
            
            //4.消费客户端 连接结束 订阅目标主题
            mqttClient.subscribe("$share/order/" + MqttTopicConst.GUN_CHECK_RESULT_TOPIC);
            mqttClient.subscribe("$share/order/" + MqttTopicConst.CHARGING_PROGRESS_TOPIC);
            log.info("已订阅主题: {} 和 {}", 
                "$share/order/" + MqttTopicConst.GUN_CHECK_RESULT_TOPIC, 
                "$share/order/" + MqttTopicConst.CHARGING_PROGRESS_TOPIC);
            
        } catch (Exception e) {
            log.error("创建MqttClient对象失败", e);
            // 连接失败时关闭客户端并抛出异常，确保不会返回未连接的客户端实例
            if (mqttClient != null) {
                try {
                    mqttClient.close();
                } catch (Exception closeException) {
                    log.error("关闭MQTT客户端失败", closeException);
                }
            }
            throw new RuntimeException("MQTT客户端初始化失败", e);
        }
        return mqttClient;
    }
}