package com.example.seckill.controller;


import com.example.seckill.pojo.User;
import com.example.seckill.service.IOrderService;
import com.example.seckill.vo.OrderDetailVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhoubin
 * @since 2022-04-15
 */
@Controller
@RequestMapping("/order")
public class OrderController {


    @Autowired
    private IOrderService orderService;

    /**
    * 订单详情
    * @Param: [usaer, orderId]
    * @return: com.example.seckill.vo.RespBean
    * @Date: 2022/8/3
    */
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);
    }

}
