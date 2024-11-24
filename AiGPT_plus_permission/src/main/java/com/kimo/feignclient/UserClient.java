//package com.kimo.feignclient;
//
//
//import com.kimo.model.po.Permissions;
//import com.kimo.model.dto.UserDto;
//import com.kimo.model.dto.UserPermissionDto;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//
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
//    @PostMapping("/auth/permission")
//    public List<Permissions> getUserPermissions(@RequestBody UserPermissionDto user);
//
//}
