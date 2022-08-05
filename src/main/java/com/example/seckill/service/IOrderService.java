package com.example.seckill.service;

import com.example.seckill.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckill.pojo.User;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhoubin
 * @since 2022-04-15
 */
public interface IOrderService extends IService<Order> {
    /**
    * 秒杀
    * @Param: [user, goodsVo]
    * @return: com.example.seckill.pojo.Order
    * @Date: 2022/7/26
    */
    Order seckill(User user, GoodsVo goodsVo);

    /**
    * 订单详情
    * @Param: [orderId]
    * @return: com.example.seckill.vo.OrderDetailVo
    * @Date: 2022/8/3
    */
    OrderDetailVo detail(Long orderId);

    /**
    * 生成秒杀地址
    * @Param: [user, goodsId]
    * @return: java.lang.String
    * @Date: 2022/8/5
    */
    String create(User user, Long goodsId);

    /**
    * 校验秒杀地址
    * @Param: [user, goodsId]
    * @return: java.lang.Boolean
    * @Date: 2022/8/5
    */
    Boolean pathCheck(User user, Long goodsId, String path);
}
