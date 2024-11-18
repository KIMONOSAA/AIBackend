//package com.kimo.config;
//
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Scope;
//
///**
// * @author Mr.kimo
// */
//@Configuration
//public class RabbitMQClient {
//
//    //省略部分非核心代码
//    @Bean
//    @Scope("prototype")
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
//        RabbitTemplate rabbitTemplate= new RabbitTemplate();
//        rabbitTemplate.setConnectionFactory(connectionFactory);
//        rabbitTemplate.setMandatory(true);
//        return rabbitTemplate;
//    }
//
//
//}
