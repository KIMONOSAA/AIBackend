package com.kimo.config;


import com.kimo.model.dto.UserDto;

/**
 * @author Mr.kimo
 */
public class UseIdHolder {
    private static final ThreadLocal<UserDto> tokenThreadLocal = new ThreadLocal<>();
  
    public static void setUserForToken(UserDto token) {
        tokenThreadLocal.set(token);  
    }  
  
    public static UserDto getUserForToken() {
        return tokenThreadLocal.get();  
    }  
  
    public static void clearUserId() {
        tokenThreadLocal.remove();  
    }  
}