package com.kimo.config;


import com.kimo.constants.CouZiConstant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
/**
 * @author Mr.kimo
 */
@Slf4j
@Component
public class OkHttpClientImpl {

    public OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(CouZiConstant.LEVEL_IN_LEVEL);
        // 2. 开启 Http 客户端
        return new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(CouZiConstant.CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(CouZiConstant.WRITE_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(CouZiConstant.READ_TIME_OUT, TimeUnit.SECONDS)
//              .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)))
                .build();
    }


}
