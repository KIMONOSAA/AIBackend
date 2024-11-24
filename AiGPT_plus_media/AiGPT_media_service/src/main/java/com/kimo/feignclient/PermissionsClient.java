//package com.kimo.feignclient;
//
//import com.kimo.model.dto.Permissions;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
///**
// * @author Mr.kimo
// * @title PermissionsClient
// * @date 2024/11/21 22:33
// * @description TODO
// */
//@FeignClient(value = "permission",url = "http://localhost:63090/permission",fallbackFactory = RolesClientFactory.class)
//public interface PermissionsClient {
//
//    @PostMapping("/get/permission")
//    public Permissions getUserPermissions(@RequestParam("roleId") String roleId);
//}
