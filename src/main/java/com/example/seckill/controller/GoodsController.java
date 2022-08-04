package com.example.seckill.controller;

import com.example.seckill.pojo.User;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IUserService;
import com.example.seckill.vo.DetailVo;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * qps:
     *  Windows优化前：848 5000*10
     *         缓存：3524 5000*10
     * @Param: [model, user]
     * @return: java.lang.String
     * @Date: 2022/8/1
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response){

//        if(StringUtils.isEmpty(ticket)){
//            return "login";
//        }
//        // User user = (User)session.getAttribute(ticket);
//        User user = userService.getUserByCookie(ticket,request,response);
//        if(null==user){
//            return "login";
//        }
        // Redis 中获取页面，如果不为空直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());
//        return "goodsList";

        // 如果为空，手动渲染，存入redis并返回
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 跳转商品详情页
     * @Param: [model, user, goodsId]
     * @return: java.lang.String
     * @Date: 2022/7/25
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId){

        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);

        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date curData = new Date();

        //秒杀状态
        int secKillStatus = 0;
        int remainSeconds = 0;

        if(curData.before(startDate)){
            //秒杀未开始
            remainSeconds = (int)((startDate.getTime() - curData.getTime())/1000);
        }else if(curData.after(endDate)){
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = 0;
        }else{

            secKillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo(
            user, goodsVo, secKillStatus, remainSeconds
        );
        return RespBean.success(detailVo);
//        return "goodsDetail";
    }

    /**
     * 跳转商品详情页
     * @Param: [model, user, goodsId]
     * @return: java.lang.String
     * @Date: 2022/7/25
     */
    @RequestMapping(value = "/toDetail_1/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail_1(Model model,User user,@PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // Redis中获取页面
        String html = ((String) valueOperations.get("goodsDetail:" + goodsId));
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);

        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date curData = new Date();

        //秒杀状态
        int secKillStatus = 0;
        int remainSeconds = 0;

        if(curData.before(startDate)){
            //秒杀未开始
            remainSeconds = (int)((startDate.getTime() - curData.getTime())/1000);
        }else if(curData.after(endDate)){
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = 0;
        }else{

            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("goods",goodsVo);

        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
//        return "goodsDetail";
    }
}
