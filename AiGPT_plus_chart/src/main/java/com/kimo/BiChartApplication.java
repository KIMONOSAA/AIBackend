package com.kimo;

import com.kimo.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
/**
 * @author Mr.kimo
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.kimo.api.client",defaultConfiguration = DefaultFeignConfig.class)
public class BiChartApplication {
    public static void main(String[] args) {
        SpringApplication.run(BiChartApplication.class, args);
    }

}