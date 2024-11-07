package com.kimo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.kimo.constant.RabbitMQConstant.*;
/**
 * @author Mr.kimo
 */
@Configuration
public class RabbitBindingConfig {

    /**
     * Direct 交换机
     */
    @Bean(DIRECT_EXCHANGE)
    public DirectExchange directExchange(){
        return new DirectExchange(DIRECT_EXCHANGE,true,false);
    }

    @Bean(DIRECT_QUEUE)
    public Queue directQueue(){
        return new Queue(DIRECT_QUEUE,true,false,false);
    }

    @Bean
    public Binding directBinding(){
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(DIRECT_ROUTING_KEY);
    }

    /**
     * Direct 交换机
     */
    @Bean(PRACTICE_DIRECT_EXCHANGE)
    public DirectExchange practiceDirectExchange(){
        return new DirectExchange(PRACTICE_DIRECT_EXCHANGE,true,false);
    }

    @Bean(PRACTICE_DIRECT_QUEUE)
    public Queue practiceDirectQueue(){
        return new Queue(PRACTICE_DIRECT_QUEUE,true,false,false);
    }

    @Bean
    public Binding practiceDirectBinding(){
        return BindingBuilder.bind(practiceDirectQueue()).to(practiceDirectExchange()).with(PRACTICE_DIRECT_ROUTING_KEY);
    }


    /**
     * Direct 交换机
     */
    @Bean(ACCURACY_DIRECT_EXCHANGE)
    public DirectExchange accuracyDirectExchange(){
        return new DirectExchange(ACCURACY_DIRECT_EXCHANGE,true,false);
    }

    @Bean(ACCURACY_DIRECT_QUEUE)
    public Queue accuracyDirectQueue(){
        return new Queue(ACCURACY_DIRECT_QUEUE,true,false,false);
    }

    @Bean
    public Binding accuracyDirectBinding(){
        return BindingBuilder.bind(accuracyDirectQueue()).to(accuracyDirectExchange()).with(ACCURACY_DIRECT_ROUTING_KEY);
    }



    //声明交换机，且持久化
    @Bean(PAYNOTIFY_EXCHANGE_FANOUT)
    public FanoutExchange paynotify_exchange_fanout() {
        // 三个参数：交换机名称、是否持久化、当没有queue与其绑定时是否自动删除
        return new FanoutExchange(PAYNOTIFY_EXCHANGE_FANOUT, true, false);
    }
    //支付通知队列,且持久化
    @Bean(PAYNOTIFY_QUEUE)
    public Queue course_publish_queue() {
        return QueueBuilder.durable(PAYNOTIFY_QUEUE).build();
    }

    //交换机和支付通知队列绑定
    @Bean
    public Binding binding_course_publish_queue() {
        return BindingBuilder.bind(course_publish_queue()).to(paynotify_exchange_fanout());
    }

}
