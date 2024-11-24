package com.kimo.interceptors;

import cn.hutool.core.util.StrUtil;
import com.kimo.utils.HeaderHolder;
import com.kimo.utils.UseIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author Mr.kimo
 * @title UserInfoInterceptor
 * @date 2024/11/23 20:17
 * @description TODO
 */
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       //获取登录用户信息
        String userInfoToken = request.getHeader("authorization");
        String header = request.getHeader("X-GatewayTokenHeader");
        if(StrUtil.isNotBlank(userInfoToken) && StrUtil.isNotBlank(header)){
            UseIdHolder.setUserForToken(userInfoToken);
            HeaderHolder.setUserForToken(header);
        }
        //判断是否获取用户，有就存入ThreadLocal
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UseIdHolder.clearUserId();
        HeaderHolder.clearUserId();
    }
}
