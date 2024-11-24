package com.kimo.utils;

/**
 * @author Mr.kimo
 * @title HeaderHolder
 * @date 2024/11/24 0:31
 * @description TODO
 */
public class HeaderHolder {
    private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();

    public static void setUserForToken(String header) {
        tokenThreadLocal.set(header);
    }

    public static String getUserForToken() {
        return tokenThreadLocal.get();
    }

    public static void clearUserId() {
        tokenThreadLocal.remove();
    }
}
