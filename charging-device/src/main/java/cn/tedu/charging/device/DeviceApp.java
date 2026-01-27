package cn.tedu.charging.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import top.javatool.canal.client.spring.boot.autoconfigure.SimpleClientAutoConfiguration;

@SpringBootApplication(exclude=SimpleClientAutoConfiguration.class)
public class DeviceApp {
    public static void main(String[] args) {
        //启动加载spring容器
        ConfigurableApplicationContext applicationContext
                = SpringApplication.run(DeviceApp.class, args);
        //run方法执行完毕,容器和应用也启动完成了
        //在这里实现缓存预热可以么? 如何在启动类main方法调用获取容器bean对象 stationMapper redisTemplate
        //applicationContext.getBean(RedisTemplate.class);
    }
}
