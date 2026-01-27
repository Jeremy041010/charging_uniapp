package cn.tedu.charging.device.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfiguration {
    //创建2个RedisTemplate对象 用不同的序列化器
    @Bean("redisTemplate")
    public RedisTemplate initRedis01(RedisConnectionFactory factory){
        RedisTemplate redisTemplate=new RedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        //自定义序列化 操作redis 操作key value 考虑hash key value field
        //key值必须在客户端传递String类型数据,然后通过底层对string字符串做序列化
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //value必须是String类型,因为客户端要使用string的序列化 String incr decr用不了了
        redisTemplate.setValueSerializer(RedisSerializer.json());
        //对于hash数据结构 单独设置序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }
    @Bean
    public GeoOperations initGeoOperations(RedisTemplate redisTemplate){
        return redisTemplate.opsForGeo();
    }
    /*//操作hash
    @Bean
    public HashOperations initHashOperations(RedisTemplate redisTemplate){
        return redisTemplate.opsForHash();
    }
    //操作字符串value
    @Bean
    public ValueOperations initValueOperations(RedisTemplate redisTemplate){
        return redisTemplate.opsForValue();
    }*/

}
