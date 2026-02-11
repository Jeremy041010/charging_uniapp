package cn.tedu.charging.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单模块启动类（修复：添加@ComponentScan扫描公共模块）
 */
@SpringBootApplication
@EnableFeignClients
// 核心修复：指定扫描根包为cn.tedu.charging，包含order（订单模块）和common（公共模块）
@ComponentScan(basePackages = {"cn.tedu.charging"})
public class OrderApp {
    public static void main(String[] args) {
        System.setProperty("spring.amqp.deserialization.trust.all","true");
        SpringApplication.run(OrderApp.class,args);
    }
}