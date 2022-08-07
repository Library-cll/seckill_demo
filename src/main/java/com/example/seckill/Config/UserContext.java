package com.example.seckill.Config;

import com.example.seckill.pojo.User;

public class UserContext {
    // 将数据保存在当前线程保证，线程安全
    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user){
        userHolder.set(user);
    }

    public static User getUser(){
        return userHolder.get();
    }
}
