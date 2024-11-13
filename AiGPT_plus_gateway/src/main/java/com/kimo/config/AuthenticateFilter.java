package com.kimo.config;




import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.constant.GatewayConstant;
import com.kimo.utils.JwtService;

import com.kimo.utils.RedisUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson.JSON;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;



/**
 * @author Mr.kimo
 */
@Slf4j
@Configuration
public class AuthenticateFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtService jwtService;


    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private RedisUtils redisUtils;

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
        //获取请求路径
        String rawPath = exchange.getRequest().getURI().getRawPath();

        if(isPv(rawPath)){
            return unauthorizedResponse(exchange, serverHttpResponse);
        }

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
        String date = LocalDateTime.now().toString();

        if(userEmail.isBlank()){
                return unauthorizedResponse(exchange, serverHttpResponse);
        }
        String jwtToken = jwtService.generateToken(date,userEmail,null);
        redisUtils.storeTokenInRedis(jwtToken,userEmail);
        ServerHttpRequest build = exchange.getRequest().mutate().header(GatewayConstant.GATEWAY_TOKEN_HEADER, jwtToken).build();
        ServerWebExchange newExchange = exchange.mutate().request(build).build();
        return chain.filter(newExchange);
    }

    /**
     * 判断是否内部私有方法
     * @param requestURI 请求路径
     * @return boolean
     */
    private boolean isPv(String requestURI) {
        return isAccess(requestURI,"/pv");
    }

    /**
     * 网关访问控制校验
     */
    private boolean isAccess(String requestURI, String access) {
        //后端标准请求路径为 /访问控制/请求路径
        int index = requestURI.indexOf(access);
        return index >= 0 && StringUtils.countOccurrencesOf(requestURI.substring(0,index),"/") < 1;
    }

}


