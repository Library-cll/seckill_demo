package com.example.seckill.exception;

import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobleExceptionhandler {

    @ExceptionHandler(Exception.class)
    public RespBean Exceptionhandler(Exception e){
        if(e instanceof GlobleException){
            GlobleException ex=(GlobleException) e;
            return RespBean.error(ex.getRespBeanEnum());
        }else if(e instanceof BindException){
            BindException ex=(BindException)e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
            respBean.setMessage("参数检验异常"+ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }else{
            return RespBean.error(RespBeanEnum.ERROR);
        }
    }
}
