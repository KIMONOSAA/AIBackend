package com.kimo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * @author Mr.kimo
 */
@Configuration
public class Executor {

        @Bean
        @Scope("prototype") // 将作用域设置为原型
        public ExecutorService executorService() {
                return new ThreadPoolExecutor(
                        24, 48, 60L,
                        TimeUnit.SECONDS, new LinkedBlockingQueue<>(150),
                        new ThreadPoolExecutor.CallerRunsPolicy()
                );
        }
}
