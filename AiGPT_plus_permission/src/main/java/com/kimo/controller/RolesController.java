package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.annotation.PermissionMethod;
import com.kimo.common.BaseResponse;
import com.kimo.common.ResultUtils;
import com.kimo.model.dto.AddRole;
import com.kimo.model.dto.UserPermissionQueryRequest;
import com.kimo.model.dto.UserRoleQueryRequest;
import com.kimo.model.po.Permissions;
import com.kimo.model.po.Roles;
import com.kimo.service.RolesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.Role;

/**
 * @author Mr.kimo
 * @title RolesController
 * @date 2024/11/21 19:52
 * @description TODO
 */
@RestController
public class RolesController {

    @Autowired
    private RolesService rolesService;

    //根据roleid获取对应信息
    @PostMapping("/get/role")
    public Roles getRoleByUserId(@RequestParam String roleId,HttpServletRequest request) {
        return rolesService.getRolesById(roleId,request);
    }

    //删除角色
    @PostMapping("/delete/role/{roleId}")
    public BaseResponse<Boolean> deletedRole(@PathVariable("roleId") Long roleId, HttpServletRequest request) {
        Boolean isBoolean = rolesService.deletedRole(roleId,request);
        return ResultUtils.success(isBoolean);
    }

    //创建角色 USER ADMIN OR MANAGER SUPERUSER
    @PostMapping("/add/role")
    public BaseResponse<Boolean> addRole(@RequestBody AddRole role, HttpServletRequest request) {
        Boolean isBoolean =  rolesService.createdRole(role,request);
        return ResultUtils.success(isBoolean);
    }

    //列出所有权限
    @PostMapping("/list/roles/page")
    /**
     * @Author kimo
     * @Description  分页获取用户只允许管理员
     * @Date
     * @Param
     * @param userQueryRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.kimo.ucenter.model.po.User>>
     **/

    public BaseResponse<Page<Roles>> listUserPermissionsByPage(@RequestBody UserRoleQueryRequest userPermissionQueryRequest,
                                                               HttpServletRequest request) {
        long current = userPermissionQueryRequest.getCurrent();
        long size = userPermissionQueryRequest.getPageSize();
        Page<Roles> userPage = rolesService.page(new Page<>(current, size),
                rolesService.getQueryWrapper(userPermissionQueryRequest,request));
        return ResultUtils.success(userPage);
    }




}
