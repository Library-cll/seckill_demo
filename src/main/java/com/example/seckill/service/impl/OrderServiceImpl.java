package com.example.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.example.seckill.exception.GlobleException;
import com.example.seckill.pojo.Order;
import com.example.seckill.mapper.OrderMapper;
import com.example.seckill.pojo.SeckillGoods;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.pojo.User;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckill.service.ISeckillGoodsService;
import com.example.seckill.service.ISeckillOrderService;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.OrderDetailVo;
import com.example.seckill.vo.RespBeanEnum;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2022-04-15
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public Order seckill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(
                new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId())
        );
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
//        seckillGoodsService.updateById(seckillGoods);
//        boolean seckillGoodsResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().
//                set("stock_count", seckillGoods.getStockCount()).
//                eq("id", seckillGoods.getId()).
//                gt("stock_count", 0));

        boolean seckillGoodsResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().
                setSql("stock_count = stock_count -1").
                eq("goods_id", goods.getId()).
                gt("stock_count", 0));

        if(seckillGoods.getStockCount() < 1){
            // 判断是否有库存
            valueOperations.set("isStockEmpty:" + goods.getId(), "0");
            return null;
        }

        // 生成订单
        Order order = new Order(
                0L, user.getId(), goods.getId(), 0L, goods.getGoodsName(), 1, seckillGoods.getSeckillPrice(),
                1, 0, new Date(), null
        );
        orderMapper.insert(order);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder(
                0L, user.getId(), order.getId(), goods.getId()
        );
        seckillOrderService.save(seckillOrder);

        redisTemplate.opsForValue().set("order"+user.getId()+":"+goods.getId(), seckillOrder);

        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null){
            throw new GlobleException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoById(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo(
                order,goodsVo
        );
        return detail;
    }
}
