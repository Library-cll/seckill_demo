package com.example.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class SeckillDemoApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Boolean>  script;

    @Test
    public void testLock01(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，key不存在才可以设置成功
        Boolean isLocck = valueOperations.setIfAbsent("k1", "v1");
        // 如果占位成功，正常进行操作
        if(isLocck){
            valueOperations.set("name", "xxxx");
            String name = ((String) valueOperations.get("name"));
            System.out.println("name:" + name);
            // 操作完成，删除锁
            redisTemplate.delete("k1");
        }else {
            System.out.printf("有线程在使用,请稍后再试");
        }
    }

    @Test
    public void testLock02(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，key不存在才可以设置成功,设置过期时间
        Boolean isLocck = valueOperations.setIfAbsent("k1", "v1", 5, TimeUnit.SECONDS);
        // 如果占位成功，正常进行操作
        if(isLocck){
            valueOperations.set("name", "xxxx");
            String name = ((String) valueOperations.get("name"));
            System.out.println("name:" + name);
            // 模拟异常
            Integer.parseInt("www");
            // 操作完成，删除锁
            redisTemplate.delete("k1");
        }else {
            System.out.println("有线程在使用,请稍后再试");
        }
    }

    @Test
    public void testLock03(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String value = UUID.randomUUID().toString();
        // 占位，key不存在才可以设置成功,设置过期时间
        Boolean isLocck = valueOperations.setIfAbsent("k1", value, 40, TimeUnit.SECONDS);
        // 如果占位成功，正常进行操作
        if(isLocck){
            valueOperations.set("name", "xxxx");
            String name = ((String) valueOperations.get("name"));
            System.out.println("name:" + name);
            System.out.println(valueOperations.get("k1"));
            Boolean result = (Boolean) redisTemplate.execute(script, Collections.singletonList("k1"), value);
            System.out.println("script结果" + result);
        }else {
            System.out.println("有线程在使用,请稍后再试");
        }
    }

}
