package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.annotation.PermissionMethod;
import com.kimo.api.dto.UserDto;
import com.kimo.common.BaseResponse;
import com.kimo.common.ResultUtils;

import com.kimo.constant.SecurityConstants;
import com.kimo.model.dto.AddPermission;
import com.kimo.model.dto.UserPermissionDto;
import com.kimo.model.dto.UserPermissionQueryRequest;
import com.kimo.model.po.Permissions;
import com.kimo.service.PermissionsService;
import com.kimo.service.RolesService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mr.kimo
 * 权限接口
 */
@RestController
@Slf4j
public class PermissionController {

    @Autowired
    PermissionsService permissionsService;

    @Autowired
    private ServletUtils servletUtils;


    //获取roleid所拥有权限
    @PostMapping("/get/permission")
    public Permissions getUserPermissions(@RequestParam String roleId,HttpServletRequest request){
        Permissions permissionByRoleId = permissionsService.getPermissionByRoleId(roleId);
//        String code = permissionByRoleId.getCode();
//        servletUtils.ensuperAdminOrAdmin(code,"114514");
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"114514");
        return permissionByRoleId;

    }

    //添加权限
    @PostMapping("/add/permission")
    public BaseResponse<Boolean> addPermission(@RequestBody AddPermission permission,HttpServletRequest request) {
        Boolean isBoolean = permissionsService.createPermission(permission,request);
        return ResultUtils.success(isBoolean);
    }





    //列出所有权限
    @PostMapping("/list/permission/page")
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

    public BaseResponse<Page<Permissions>> listUserPermissionsByPage(@RequestBody UserPermissionQueryRequest userPermissionQueryRequest,
                                                   HttpServletRequest request) {
        long current = userPermissionQueryRequest.getCurrent();
        long size = userPermissionQueryRequest.getPageSize();
        Page<Permissions> userPage = permissionsService.page(new Page<>(current, size),
                permissionsService.getQueryWrapper(userPermissionQueryRequest,request));
        return ResultUtils.success(userPage);
    }

    //列出所有用户对应的权限





    //删除全部权限
    @PostMapping("/delete/permission/{permissionId}")
    public BaseResponse<Boolean> deletedPermission(@PathVariable("permissionId") Long permissionId,HttpServletRequest request) {
        Boolean isBoolean = permissionsService.deletedPermission(permissionId,request);
        return ResultUtils.success(isBoolean);
    }

    //删除权限
    @PostMapping("/delete/permission/list/{permissionId}")
    public BaseResponse<Boolean> deletedPermissionForOne(@PathVariable("permissionId") Long permissionId,@RequestParam("roleCode") String roleCode,HttpServletRequest request) {
        Boolean isBoolean = permissionsService.deletedPermissionForOne(permissionId,roleCode,request);
        return ResultUtils.success(isBoolean);
    }



    //查找当前用户权限
    @GetMapping("/get/permission/{permissionId}")
    public BaseResponse<Permissions> getPermission(@PathVariable("permissionId") Long permissionId,HttpServletRequest request) {
        Permissions permissions = permissionsService.getPermission(permissionId,request);
        return ResultUtils.success(permissions);
    }


}
