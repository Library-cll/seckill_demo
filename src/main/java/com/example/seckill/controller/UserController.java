package com.example.seckill.controller;


import com.example.seckill.pojo.User;
import com.example.seckill.rabbitmq.MQSender;
import com.example.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhoubin
 * @since 2022-04-10
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    /**
    * 测试（用户信息）
    * @Param: [user]
    * @return: com.example.seckill.vo.RespBean
    * @Date: 2022/8/1
    */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }


    /**
    * MQ测试发送
    * @Param: []
    * @return: void
    * @Date: 2022/8/3
    */
    @RequestMapping("/mq")
    @ResponseBody
    public void mq(){
        mqSender.send("hello");
    }

    /**
    * fanout 模式
    * @Param: []
    * @return: void
    * @Date: 2022/8/3
    */
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq_fanout(){
        mqSender.send("hello");
    }

    @RequestMapping("/mq/direct01")
    @ResponseBody
    public void mq_direct01(){
        mqSender.sendDirect01("hello red");
    }

    @RequestMapping("/mq/direct02")
    @ResponseBody
    public void mq_direct02(){
        mqSender.sendDirect02("hello green");
    }

    @RequestMapping("/mq/topic01")
    @ResponseBody
    public void topic01(){
        mqSender.sendTopic01("hello red");
    }

    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void topic02(){
        mqSender.sendTopic02("hello green");
    }

    @RequestMapping("/mq/headers01")
    @ResponseBody
    public void headers01(){
        mqSender.sendHeaders01("hello headers01");
    }

    @RequestMapping("/mq/headers02")
    @ResponseBody
    public void headers02(){
        mqSender.sendHeaders02("hello headers02");
    }
}
