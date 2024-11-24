//package com.kimo.feignclient;
//
//import com.kimo.model.dto.Roles;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
///**
// * @author Mr.kimo
// * @title RolesClient
// * @date 2024/11/21 22:33
// * @description TODO
// */
//@FeignClient(value = "permission",url = "http://localhost:63090/permission",fallbackFactory = RolesClientFactory.class)
//public interface RolesClient {
//
//    @PostMapping("/get/role")
//    public Roles getRoleByUserId(@RequestParam("roleId") String roleId);
//}
