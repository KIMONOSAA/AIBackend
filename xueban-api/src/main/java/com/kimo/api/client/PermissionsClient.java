package com.kimo.api.client;


import com.kimo.api.dto.Permissions;
import com.kimo.api.dto.Roles;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Mr.kimo
 * @title PermissionsClient
 * @date 2024/11/21 22:33
 * @description TODO
 */
@FeignClient(value = "permission",url = "http://localhost:63090/permission")
public interface PermissionsClient {

    @PostMapping("/get/permission")
    public Permissions getUserPermissions(@RequestParam("roleId") String roleId);

    @PostMapping("/get/role")
    public Roles getRoleByUserId(@RequestParam("roleId") String roleId);
}
