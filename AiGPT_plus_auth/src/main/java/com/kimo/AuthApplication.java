package com.kimo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication

@EnableFeignClients(basePackages = "com.kimo.ucenter.feignclient")
//@MapperScan("com.kimo.ucenter.mapper")
public class AuthApplication  {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
