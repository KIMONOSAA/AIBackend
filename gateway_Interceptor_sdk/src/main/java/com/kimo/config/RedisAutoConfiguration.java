package com.kimo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties({RedisProperties.class,CloudProperties.class})
@Import(CloudInterceptorConfigure.class)
public class RedisAutoConfiguration {



    private final RedisProperties redisProperties;

//    @Bean
//    public CloudInterceptorConfigure cloudSecurityInterceptorConfigure() {
//        return new CloudInterceptorConfigure();
//    }

//    @PostConstruct
//    public void checkRedisTemplateExistence() {
//        try {
//            Class.forName("org.springframework.data.redis.core.RedisTemplate");
//            System.out.println("RedisTemplate is present in the classpath.");
//        } catch (ClassNotFoundException e) {
//            System.out.println("RedisTemplate is NOT present in the classpath.");
//        }
//    }



    public RedisAutoConfiguration(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Create a LettuceConnectionFactory with the desired host and port
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
                redisProperties.getHost(), redisProperties.getPort());

        // Set the database index to 1
        connectionFactory.setDatabase(1);

        // If there's a password, set it
        connectionFactory.setPassword(redisProperties.getPassword());

        // Initialize the connection factory
        connectionFactory.afterPropertiesSet();

        return connectionFactory;
    }

    @Bean
    @Qualifier("redisTemplateOrGateway")
    public RedisTemplate<String, String> redisTemplateOrGateway(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 配置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }


//    @Bean
//    public RedisOrGatewayUtils redisOrGatewayUtils(StringRedisTemplate redisTemplate) {
//        return new RedisOrGatewayUtils(redisTemplate);
//    }
}