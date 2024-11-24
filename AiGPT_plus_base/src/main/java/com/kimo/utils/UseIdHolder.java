package com.kimo.utils;


import com.kimo.model.UserDto;

/**
 * 用于存储用户信息
 * @author  kimo
 */
public class UseIdHolder {
    private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();
  
    public static void setUserForToken(String token) {
        tokenThreadLocal.set(token);  
    }  
  
    public static String getUserForToken() {
        return tokenThreadLocal.get();  
    }  
  
    public static void clearUserId() {
        tokenThreadLocal.remove();  
    }  
}