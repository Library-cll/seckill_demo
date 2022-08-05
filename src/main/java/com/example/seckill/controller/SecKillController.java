package com.example.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill.pojo.Order;
import com.example.seckill.pojo.SeckillMessage;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.pojo.User;
import com.example.seckill.rabbitmq.MQSender;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IOrderService;
import com.example.seckill.service.ISeckillOrderService;
import com.example.seckill.utils.JsonUtil;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/secKill")
public class SecKillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    /**
     * Windws 优化前qps：549 5000*10
     *        缓存+页面静态化 950
     *        +MQ,+缓存 1334
     * @Param: [model, user, goodsId]
     * @return: java.lang.String
     * @Date: 2022/8/2
     */
    @RequestMapping(value = "/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(Model model, User user, Long goodsId){
        if(null == user){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 判断是否重复抢购
        SeckillOrder seckillOrder =  ((SeckillOrder) redisTemplate.opsForValue().get("order" + user.getId() + ":" + goodsId));
        if(seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // 内存标记减少访问Redis
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 减一之后的库存 预减库存

        // 旧写法
        // Long stock = valueOperations.decrement("secKillGoods:" + goodsId);

        // 使用lua脚本
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("secKillGoods:" + goodsId), Collections.EMPTY_LIST);

        if(stock < 0){
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("secKillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);



        /**
        * 旧版本
        */
/*        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        // 判断库存
        if(goodsVo.getStockCount() < 1){
//            model.addAttribute("errmasg", RespBeanEnum.EMPTY_STOCK.getMessge());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
//        // 判断是否重复抢购
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
//                eq("user_id", user.getId()).eq("goods_id", goodsId));

        SeckillOrder seckillOrder =  ((SeckillOrder) redisTemplate.opsForValue().get("order" + user.getId() + ":" + goodsId));

        if(seckillOrder != null){
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessge());
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        Order order = orderService.seckill(user, goodsVo);*/

    }

    /**
     * Windws 优化前qps：549 5000*10
     * @Param: [model, user, goodsId]
     * @return: java.lang.String
     * @Date: 2022/8/2
     */
    @RequestMapping(value = "/doSecKill_1")
    public String doSecKill_1(Model model, User user, Long goodsId){
        if(null == user){
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        // 判断库存
        if(goodsVo.getStockCount() < 1){
            model.addAttribute("errmasg", RespBeanEnum.EMPTY_STOCK.getMessge());
            return "secKillFail";
        }
        // 判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
                eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrder != null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessge());
            return "secKillFail";
        }
        Order order = orderService.seckill(user, goodsVo);
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        return "orderDetail";
    }

    /**
    * 获取秒杀结果
    * @Param: [user, goodsId]
    * @return: com.example.seckill.vo.RespBean orderId：成功;-1：秒杀失败;0：排队中
    * @Date: 2022/8/4
    */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }


    /**
    * 系统初始化时执行，把所有商品库存同步到redis中
    * @Param: []
    * @return: void
    * @Date: 2022/8/4
    */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(goodsList)){
            return;
        }
        goodsList.forEach(goodsVo ->{
            redisTemplate.opsForValue().set("secKillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }
}
