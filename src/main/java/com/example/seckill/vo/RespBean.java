package com.example.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {
    private long code;
    private String message;
    private Object object;

    //成功返回结果
    public static RespBean success(){
        return  new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBeanEnum.SUCCESS.getMessge(),null);
    }

    public static RespBean success(Object o){
        return  new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBeanEnum.SUCCESS.getMessge(),o);
    }

    //失败
    public static RespBean error(RespBeanEnum respBeanEnum){
        return  new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessge(),null);
    }

    public static RespBean success(RespBeanEnum respBeanEnum,Object o){
        return  new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessge(),o);
    }

    public static void main(String[] args) {
        RespBeanEnum respBeanEnum=RespBeanEnum.ERROR;
        System.out.println(error(respBeanEnum));
    }
}
