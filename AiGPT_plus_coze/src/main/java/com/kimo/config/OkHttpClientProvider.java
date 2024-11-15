package com.kimo.config;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

public class OkHttpClientProvider {

    // 静态 OkHttpClient 实例，确保只有一个客户端实例
    private static OkHttpClient client;

    private OkHttpClientProvider() {
        // 私有化构造函数，确保外部无法直接实例化
    }

    public static OkHttpClient getClient() {
        // 如果 client 是 null，初始化它
        if (client == null) {
            synchronized (OkHttpClientProvider.class) {
                if (client == null) {
                    // 创建 HttpLoggingInterceptor，输出请求和响应的日志
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // 创建 OkHttpClient，并设置一些常见的配置
                    client = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间
                            .writeTimeout(10, TimeUnit.SECONDS)    // 设置写入超时时间
                            .readTimeout(30, TimeUnit.SECONDS)     // 设置读取超时时间
                            .addInterceptor(loggingInterceptor)    // 添加日志拦截器
                            .build();
                }
            }
        }
        return client;
    }
}
