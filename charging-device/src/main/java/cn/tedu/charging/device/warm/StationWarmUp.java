package cn.tedu.charging.device.warm;

import cn.tedu.charging.device.dao.mapper.StationMapper;
import cn.tedu.charging.device.service.WarmUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.stereotype.Component;

/**
 * ApplicationRunner 接口是在容器管理bean的流程中
 * 接近末尾生效的
 */
@Component
@Slf4j
public class StationWarmUp implements ApplicationRunner {
    /*
        run方法是在当前bean对象的生命周期末尾调用执行的,他执行的时候容器基本已经启动结束了
        回顾bean生命周期的过程步骤
        1. 实例化Bean(new 反射创建对象)
        2. 属性填充(注入)
        3. @PostConstruct构造(利用属性 将使用一些资源创建)
        4. 自定义init(一般情况下 只需要@PostConstruct)
        5. 分支容器会判断当前bean对象是否是单例
        5.1 不是单例 bean准备就绪了
        5.2 是单例 判断当前的bean是否继承了2个接口ApplicationRunner,CommandLineRunner
        6.容器启动结束
        关闭 销毁
        @PreDestroy
        自定义destroy
     */
    @Autowired
    private WarmUpService warmUpService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //run方法执行的时候是容器启动结束的末尾,表示我们可以根据需求,随时调用任何容器的bean对象来
        //实现业务
        log.debug("缓存预热接口开始执行预热功能");
        warmUpService.doWarmUp();
        log.debug("缓存预热接口执行完毕");
    }
}
