package com.example.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill.Config.AccessLimit;
import com.example.seckill.exception.GlobleException;
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
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Slf4j
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
     * Windws ?????????qps???549 5000*10
     *        ??????+??????????????? 950
     *        +MQ,+?????? 1334
     * @Param: [model, user, goodsId]
     * @return: java.lang.String
     * @Date: 2022/8/2
     */
    @RequestMapping(value = "/{path}/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, User user, Long goodsId){
        if(null == user){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();

        // ????????????????????????
        Boolean check = orderService.pathCheck(user, goodsId, path);
        if(!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        // ????????????????????????
        SeckillOrder seckillOrder =  ((SeckillOrder) redisTemplate.opsForValue().get("order" + user.getId() + ":" + goodsId));
        if(seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // ????????????????????????Redis
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // ????????????????????? ????????????

        // ?????????
        // Long stock = valueOperations.decrement("secKillGoods:" + goodsId);

        // ??????lua??????
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
        * ?????????
        */
/*        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        // ????????????
        if(goodsVo.getStockCount() < 1){
//            model.addAttribute("errmasg", RespBeanEnum.EMPTY_STOCK.getMessge());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
//        // ????????????????????????
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
     * Windws ?????????qps???549 5000*10
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
        // ????????????
        if(goodsVo.getStockCount() < 1){
            model.addAttribute("errmasg", RespBeanEnum.EMPTY_STOCK.getMessge());
            return "secKillFail";
        }
        // ????????????????????????
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
    * ??????????????????
    * @Param: [user, goodsId]
    * @return: com.example.seckill.vo.RespBean orderId?????????;-1???????????????;0????????????
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
    * ??????????????????&???????????????
    * @Param: [user, goodsId]
    * @return: com.example.seckill.vo.RespBean
    * @Date: 2022/8/5
    */
    @AccessLimit(second=5, maxCount=5, needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getSecKillPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        // ???????????????
        Boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        // ????????????????????????
        String secKillPath = orderService.create(user, goodsId);
        return RespBean.success(secKillPath);
    }

    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if(user == null || goodsId < 0){
            throw new GlobleException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // ?????????????????????????????????
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //???????????????
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 30, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(),
                300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("?????????????????????", e.getMessage());
        }


    }

    /**
    * ?????????????????????????????????????????????????????????redis???
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
