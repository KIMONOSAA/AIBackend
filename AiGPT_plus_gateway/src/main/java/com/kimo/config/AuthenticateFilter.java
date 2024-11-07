package com.kimo.config;




import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.utils.JwtService;

import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson.JSON;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
/**
 * @author Mr.kimo
 */
@Slf4j
@Configuration
public class AuthenticateFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtService jwtService;


    /**
     * 将 JWT 鉴权失败的消息响应给客户端
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, ServerHttpResponse serverHttpResponse) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        BaseResponse responseResult = ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory()
                .wrap(JSON.toJSONStringWithDateFormat(responseResult, JSON.DEFFAULT_DATE_FORMAT)
                        .getBytes(StandardCharsets.UTF_8));
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }


    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        ServerHttpRequest.Builder mutate = serverHttpRequest.mutate();
        String requestUrl = serverHttpRequest.getURI().getPath();



        final String header = serverHttpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        final String jwt;
        final String userEmail;
        if(header == null || !header.startsWith("Bearer ")){
            return chain.filter(exchange);
        }
        if(requestUrl.contains("/auth")){
            return chain.filter(exchange);
        }
        jwt = header.substring(7);
            if (StringUtil.isEmpty(jwt)){
               return unauthorizedResponse(exchange, serverHttpResponse);
            }

        if (!jwtService.isTokenValid(jwt)){
                return unauthorizedResponse(exchange, serverHttpResponse);
        }
        userEmail = jwtService.extractUsername(jwt);
        if(userEmail.isBlank()){
                return unauthorizedResponse(exchange, serverHttpResponse);
        }

        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }
}


