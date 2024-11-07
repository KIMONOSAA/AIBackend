package com.kimo.controller;

import com.kimo.ucenter.model.dto.UserDto;
import com.kimo.ucenter.model.dto.UserPermissionDto;
import com.kimo.ucenter.model.po.Permissions;
import com.kimo.ucenter.model.po.User;
import com.kimo.ucenter.service.PermissionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mr.kimo
 * 权限接口
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class PermissionController {

    @Autowired
    PermissionsService permissionsService;

    @PostMapping("/permission")
    public List<Permissions> getUserPermissions(@RequestBody UserPermissionDto user){
        return permissionsService.getUserPermissions(user);
    }

}
