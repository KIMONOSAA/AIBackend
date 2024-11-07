
package com.kimo.core;


public class CoZeApiKeyProvider {
    /**
     * api密钥
     */
    private static volatile String apiKey;
    
    /**
     * 获取api密钥
     *
     * @return api密钥
     */
    public static String getApiKey() {
        return apiKey;
    }
    
    /**
     * 设置api密钥
     *
     * @param newApiKey 新api密钥
     */
    public static void setApiKey(String newApiKey) {
        apiKey = newApiKey;
    }
}
