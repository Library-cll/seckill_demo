package com.example.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(Object msg){
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("fanoutExchange","", msg);
    }

    public void sendDirect01(Object msg){
        log.info("发送red消息：" + msg);
        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
    }

    public void sendDirect02(Object msg){
        log.info("发送green消息：" + msg);
        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);
    }

    public void sendTopic01(Object msg){
        log.info("发送消息QUEUE01接收：" + msg);
        rabbitTemplate.convertAndSend("topicExchange", "queue.red.msg", msg);
    }

    public void sendTopic02(Object msg){
        log.info("发送消息QUEUE01&QUEUE02接收：" + msg);
        rabbitTemplate.convertAndSend("topicExchange", "green.queue.red.msg", msg);
    }

    public void sendHeaders01(String msg){
        log.info("发送消息QUEUE01&QUEUE02接收：" + msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("color", "red");
        messageProperties.setHeader("speed", "fast");
        Message message = new Message(msg.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }

    public void sendHeaders02(String msg){
        log.info("发送消息QUEUE01接收：" + msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("color", "red");
        messageProperties.setHeader("speed", "normal");
        Message message = new Message(msg.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }

    /**
    * 发送秒杀信息
    * @Param: [message]
    * @return: void
    * @Date: 2022/8/4
    */
    public void sendSecKillMessage(String message){
        log.info("发送秒杀信息："+message);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", message);
    }
}
