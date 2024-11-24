//package com.kimo.ucenter.service;
//
//
//import com.baomidou.mybatisplus.core.conditions.Wrapper;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.kimo.ucenter.model.dto.AddPermission;
//import com.kimo.ucenter.model.dto.UserPermissionDto;
//import com.kimo.ucenter.model.dto.UserPermissionQueryRequest;
//import com.kimo.ucenter.model.po.Permissions;
//import jakarta.servlet.http.HttpServletRequest;
//
//
//import java.util.List;
//
///**
// * @Author kimo
// * @Description  权限服务
// * @Date
// * @Param
// * @return
// * @return null
// **/
//public interface PermissionsService extends IService<Permissions> {
//    /**
//     * @Author kimo
//     * @Description  获取用户权限
//     * @Date
//     * @Param
//     * @return
//     * @return null
//     **/
//    public List<Permissions> getUserPermissions(UserPermissionDto user);
//
//
//    Wrapper<Permissions> getQueryWrapper(UserPermissionQueryRequest userPermissionQueryRequest);
//
//    Boolean bindPermission(HttpServletRequest request);
//
//    Boolean createPermission(AddPermission permission);
//
//    Boolean deletedPermission(Long permissionId, HttpServletRequest request);
//
//    Permissions getPermission(Long permissionId, HttpServletRequest request);
//}
