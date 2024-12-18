package com.kimo;

import com.kimo.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;



@SpringBootApplication
@EnableFeignClients(basePackages = "com.kimo.api.client",defaultConfiguration = DefaultFeignConfig.class)
@EnableDiscoveryClient
/**
 * @Author: Mr.kimo
 * @Date: 18:27
 * @return:
 * @Param:
 * @Description: 当前模块大部分已弃用
 */
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}