package cn.tedu.charging.order.config;

import cn.tedu.charging.common.utils.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 在不同的进程启动的时候
 * 加载创建满足雪花生成的bean对象 由于每一个进程最终都要运行在服务器
 * 所以创建的时候 要指定数据中心的id和机器id
 */
@Configuration
public class SnowflakeConfiguration {
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(){
        //初始化生成器 假设当前运行进程的数据中心是1 机器id也是1
        return new SnowflakeIdGenerator(1,1);
    }
}
