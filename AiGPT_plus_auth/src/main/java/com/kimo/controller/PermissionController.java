//package com.kimo.controller;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.kimo.annotation.PermissionMethod;
//import com.kimo.common.BaseResponse;
//import com.kimo.common.ResultUtils;
//import com.kimo.ucenter.model.dto.*;
//import com.kimo.ucenter.model.po.Permissions;
//import com.kimo.ucenter.model.po.User;
//import com.kimo.ucenter.service.PermissionsService;
//import com.kimo.ucenter.service.RolesService;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * @author Mr.kimo
// * 权限接口
// */
//@RestController
//@RequestMapping("/auth")
//@Slf4j
//public class PermissionController {
//
//    @Autowired
//    PermissionsService permissionsService;
//
//    @Autowired
//    RolesService rolesService;
//
//    @PostMapping("/permission")
//    public List<Permissions> getUserPermissions(@RequestBody UserPermissionDto user){
//        return permissionsService.getUserPermissions(user);
//    }
//
//    //添加权限
//    @PostMapping("/add/permission")
//    public BaseResponse<Boolean> addPermission(@RequestBody AddPermission permission) {
//        Boolean isBoolean = permissionsService.createPermission(permission);
//        return ResultUtils.success(isBoolean);
//    }
//
//
//    //绑定权限
//    @PostMapping("/bind/permission")
//    public BaseResponse<Boolean> bindPermission(HttpServletRequest request){
//        Boolean isBoolean =  permissionsService.bindPermission(request);
//        return ResultUtils.success(isBoolean);
//    }
//
//
//    //列出所有权限
//    @PostMapping("/list/permission/page")
//    @PermissionMethod(permission = "user_read_all")
//    /**
//     * @Author kimo
//     * @Description  分页获取用户只允许管理员
//     * @Date
//     * @Param
//     * @param userQueryRequest
//     * @param request
//     * @return
//     * @return com.kimo.common.BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.kimo.ucenter.model.po.User>>
//     **/
//
//    public BaseResponse<Page<Permissions>> listUserPermissionsByPage(@RequestBody UserPermissionQueryRequest userPermissionQueryRequest,
//                                                   HttpServletRequest request) {
//        long current = userPermissionQueryRequest.getCurrent();
//        long size = userPermissionQueryRequest.getPageSize();
//        Page<Permissions> userPage = permissionsService.page(new Page<>(current, size),
//                permissionsService.getQueryWrapper(userPermissionQueryRequest));
//        return ResultUtils.success(userPage);
//    }
//
//    //列出所有用户对应的权限
//
//
//    //删除角色
//    @PostMapping("/delete/role/{roleId}")
//    public BaseResponse<Boolean> deletedRole(@RequestPart("roleId") Long roleId, HttpServletRequest request) {
//        Boolean isBoolean = rolesService.deletedRole(roleId,request);
//        return ResultUtils.success(isBoolean);
//    }
//
//
//    //删除权限
//    @PostMapping("/delete/permission/{permissionId}")
//    public BaseResponse<Boolean> deletedPermission(@RequestPart("permissionId") Long permissionId,HttpServletRequest request) {
//        Boolean isBoolean = permissionsService.deletedPermission(permissionId,request);
//        return ResultUtils.success(isBoolean);
//    }
//
//
//
//    //查找当前用户权限
//    @GetMapping("/get/permission/{permissionId}")
//    public BaseResponse<Permissions> getPermission(@RequestPart("permissionId") Long permissionId,HttpServletRequest request) {
//        Permissions permissions = permissionsService.getPermission(permissionId,request);
//        return ResultUtils.success(permissions);
//    }
//
//
//}
