package com.example.seckill.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
         RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
         // key序列化
         redisTemplate.setKeySerializer(new StringRedisSerializer());
         // value序列化
         redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
         // hash key
         redisTemplate.setHashKeySerializer(new StringRedisSerializer());
         // hash value
         redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 注入连接工厂
         redisTemplate.setConnectionFactory(redisConnectionFactory);
         return redisTemplate;
    }

    @Bean
    public DefaultRedisScript<Boolean> scrip(){
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        // 脚本位置和
        script.setLocation(new ClassPathResource("lock.lua"));
        script.setResultType(Boolean.class);
        return script;
    }
}
