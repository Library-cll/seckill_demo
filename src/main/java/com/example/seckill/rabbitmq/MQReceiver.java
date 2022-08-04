package com.example.seckill.rabbitmq;

import com.example.seckill.pojo.SeckillMessage;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.pojo.User;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IOrderService;
import com.example.seckill.service.ISeckillGoodsService;
import com.example.seckill.utils.JsonUtil;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(queues = "queue")
    public void receive0(Object msg){
        log.info("接受消息：" + msg);
    }

    @RabbitListener(queues = "queue_fanout01")
    public void reveive01(Object msg){
        log.info("QUEUE01 接收消息："+msg);
    }

    @RabbitListener(queues = "queue_fanout02")
    public void reveive02(Object msg){
        log.info("QUEUE02 接收消息："+msg);
    }

    @RabbitListener(queues = "queue_direct01")
    public void receive03(Object msg){
        log.info("QUEUE01接收消息："+msg);
    }

    @RabbitListener(queues = "queue_direct02")
    public void receive04(Object msg){
        log.info("QUEUE02接收消息："+msg);
    }

    @RabbitListener(queues = "queue_topic01")
    public void receive05(Object msg){
        log.info("QUEUE01接收消息");
    }

    @RabbitListener(queues = "queue_topic02")
    public void receive06(Object msg){
        log.info("QUEUE02接收消息");
    }

    @RabbitListener(queues = "queue_headers01")
    public void receive07(Message message){
        log.info("QUEUE01接收消息对象:"+message);
        log.info("QUEUE01接收消息:"+new String(message.getBody()));

    }

    @RabbitListener(queues = "queue_headers02")
    public void receive08(Message message){
        log.info("QUEUE02接收消息对象:"+message);
        log.info("QUEUE02接收消息:"+new String(message.getBody()));

    }
    /**
    * 下单操作
    * @Param: [message]
    * @return: void
    * @Date: 2022/8/4
    */
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("接收消息："+message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        // 检查库存
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        if(goodsVo.getStockCount() < 1){
            return;
        }
        // 判断是否重复抢购
        SeckillOrder seckillOrder =  ((SeckillOrder) redisTemplate.opsForValue().get("order" + user.getId() + ":" + goodsId));
        if(seckillOrder != null){
            return ;
        }
        // 下单
        orderService.seckill(user, goodsVo);
    }
}
