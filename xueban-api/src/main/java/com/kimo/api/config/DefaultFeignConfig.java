package com.kimo.api.config;

import com.kimo.utils.HeaderHolder;
import com.kimo.utils.UseIdHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

/**
 * @author Mr.kimo
 * @title DefaultFeignConfig
 * @date 2024/11/23 21:06
 * @description TODO
 */
public class DefaultFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String userForToken = UseIdHolder.getUserForToken();
                String userForToken1 = HeaderHolder.getUserForToken();
                template.header("authorization", userForToken);
                template.header("X-GatewayTokenHeader", userForToken1);
            }
        };
    }
}
