package com.example.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class SeckillDemoApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testLock01(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，key不存在才可以设置成功
        Boolean isLocck = valueOperations.setIfAbsent("k1", "v1");
        // 如果占位成功，正常进行操作
        if(isLocck){
            valueOperations.set("name", "xxxx");
            String name = ((String) valueOperations.get("name"));
            System.out.printf("name:" + name);
            // 操作完成，删除锁
            redisTemplate.delete("name");
        }else {
            System.out.printf("有线程在使用");
        }
    }

}
