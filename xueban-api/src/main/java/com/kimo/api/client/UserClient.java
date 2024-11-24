package com.kimo.api.client;


import com.kimo.api.dto.Permissions;
import com.kimo.api.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Mr.kimo
 */
@FeignClient(value = "auth",url = "http://localhost:63090/auth",fallbackFactory = UserClientFactory.class)
public interface UserClient {

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @PostMapping("/auth/gobalget/login")
    public UserDto GobalGetLoginUser(@RequestParam("request") String request);


    @PostMapping("/auth/isAdmin")
    public Boolean isAdmin(HttpServletRequest request);


    @PostMapping("/auth/update/point")
    public Boolean updatePoint(@RequestParam("userId") Long userId,@RequestParam("point") Long point);


//    @PostMapping("/auth/permission")
//    public List<Permissions> getUserPermissions(@RequestBody UserPermissionDto user);
}
