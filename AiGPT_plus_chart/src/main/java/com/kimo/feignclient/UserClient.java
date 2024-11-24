//package com.kimo.feignclient;
//
//import com.kimo.model.dto.chart.UserDto;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
///**
// * @author Mr.kimo
// */
//@FeignClient(value = "auth",url = "http://localhost:63090/auth",fallbackFactory = UserClientFactory.class)
//public interface UserClient {
//
//    /**
//     * 获取当前登录用户
//     *
//     * @param request
//     * @return
//     */
//    @PostMapping("/auth/gobalget/login")
//    public UserDto GobalGetLoginUser(@RequestParam("request") String request);
//
//
//    @PostMapping("/auth/isAdmin")
//    public Boolean isAdmin(HttpServletRequest request);
//
//
//    @PostMapping("/auth/update/point")
//    public Boolean updatePoint(@RequestParam("userId") Long userId,@RequestParam("point") Long point);
//
//
//}
