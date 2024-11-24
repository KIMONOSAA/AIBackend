package com.kimo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@EnableFeignClients
@EnableFeignClients  //入口类加入注解开启OpenFeign支持
@EnableDiscoveryClient  //都是能够让注册中心能够发现，扫描到该服务。
public class CheckcodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckcodeApplication.class, args);
    }

}
