package com.kimo.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.kimo.constant.RabbitMQConstant.*;
import static com.kimo.constant.RabbitMQConstant.PAYNOTIFY_QUEUE;

/**
 * @author Mr.kimo
 * @title CommonConfig
 * @date 2024/11/18 9:01
 * @description TODO
 */
@Configuration
@ComponentScan("com.kimo.config")
public class CommonConfig {

    @Bean
    public Cache<String, Object> commonCaffeine() {
        return Caffeine.newBuilder()
                //初始大小
                .initialCapacity(1000)
                //PS：expireAfterWrite和expireAfterAccess同时存在时，以expireAfterWrite为准。
                //最后一次写操作后经过指定时间过期
//                .expireAfterWrite(Duration.ofMinutes(30))
                //最后一次读或写操作后经过指定时间过期
                .expireAfterAccess(Duration.ofHours(16))
                // 最大数量，默认基于缓存内的元素个数进行驱逐
                .maximumSize(10000)
                //打开数据收集功能  hitRate(): 查询缓存的命中率 evictionCount(): 被驱逐的缓存数量 averageLoadPenalty(): 新值被载入的平均耗时
//                .recordStats()
                .build();
    }

    @Bean
    @Scope("prototype") // 将作用域设置为原型
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(
                24, 48, 60L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(150),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Bean
    public OpenAPI springShopOpenApi() {
        return new OpenAPI()
                // 接口文档标题
                .info(new Info().title("kimo")
                        // 接口文档简介
                        .description("这是基于Knife4j OpenApi3的测试接口文档")
                        // 接口文档版本
                        .version("1.0版本")
                        // 开发者联系方式
                        .contact(new Contact().name("kimo")
                                .email("skimo8092@gmail.com")));

    }

    //省略部分非核心代码
    @Bean
    @Scope("prototype")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate= new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

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


    @Bean(ORDER_EXCHANGE)
    public DirectExchange order_exchange() {
        return new DirectExchange(ORDER_EXCHANGE,true,false);
    }


    @Bean(ORDER_QUEUE)
    public Queue order_queue() {
        return new Queue(ORDER_QUEUE,true,false,false);
    }


    @Bean
    public Binding binding_order_queue() {
        return BindingBuilder.bind(order_queue()).to(order_exchange()).with(ORDER_ROUTING_KEY);
    }

    @Bean(DEAD_LETTER_EXCHANGE)
    public DirectExchange order_ttl_exchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE,true,false);
    }

    @Bean(DEAD_LETTER_QUEUE)
    public Queue order_ttl_queue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE)
                .withArgument("x-dead-letter-exchange",ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",ORDER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding binding_dead_letter_queue() {
        return BindingBuilder.bind(order_ttl_queue()).to(order_ttl_exchange()).with(DEAD_LETTER_ROUTING_KEY);
    }




}
