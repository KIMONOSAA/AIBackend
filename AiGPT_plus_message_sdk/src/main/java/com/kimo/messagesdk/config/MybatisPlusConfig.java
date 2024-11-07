package com.kimo.messagesdk.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.kimo
 */
@Configuration("messagesdk_mpconfig")
@MapperScan("com.kimo.messagesdk.mapper")
public class MybatisPlusConfig {


}