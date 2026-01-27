package cn.tedu.charging.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderApp {
    public static void main(String[] args) {
        System.setProperty("spring.amqp.deserialization.trust.all","true");
        SpringApplication.run(OrderApp.class,args);
    }
}
