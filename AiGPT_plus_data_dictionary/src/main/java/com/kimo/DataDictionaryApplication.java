package com.kimo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kimo.mapper")
public class DataDictionaryApplication  {
    public static void main(String[] args) {
        SpringApplication.run(DataDictionaryApplication.class, args);
    }

}