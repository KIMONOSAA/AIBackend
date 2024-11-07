//package com.kimo.config;
//
//
//import com.kimo.constants.CouZiConstant;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Configuration
//public class OkHttpClientImpl {
//
//    @Bean
//    public OkHttpClient okHttpClient() {
//        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//        httpLoggingInterceptor.setLevel(CouZiConstant.LEVEL_IN_LEVEL);
//        // 2. 开启 Http 客户端
//        return new OkHttpClient.Builder()
//                .addInterceptor(httpLoggingInterceptor)
//                .connectTimeout(CouZiConstant.CONNECT_TIME_OUT, TimeUnit.SECONDS)
//                .writeTimeout(CouZiConstant.WRITE_TIME_OUT, TimeUnit.SECONDS)
//                .readTimeout(CouZiConstant.READ_TIME_OUT, TimeUnit.SECONDS)
//                .build();
//    }
//
//
//}
