package com.kimo;


import com.kimo.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.kimo.api.client",defaultConfiguration = DefaultFeignConfig.class)
public class PracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(PracticeApplication.class, args);
    }
}