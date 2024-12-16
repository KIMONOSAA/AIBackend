package com.kimo.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import com.kimo.api.dto.UserDto;
import com.kimo.model.dto.AddPermission;

import com.kimo.model.dto.UserPermissionDto;
import com.kimo.model.dto.UserPermissionQueryRequest;
import com.kimo.model.po.Permissions;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @Author kimo
 * @Description  权限服务
 * @Date
 * @Param
 * @return
 * @return null
 **/
public interface PermissionsService extends IService<Permissions> {
    /**
     * @Author kimo
     * @Description  获取用户权限
     * @Date
     * @Param
     * @return
     * @return null
     **/
    public List<Permissions> getUserPermissions(UserPermissionDto user);


    Wrapper<Permissions> getQueryWrapper(UserPermissionQueryRequest userPermissionQueryRequest,HttpServletRequest request);

    Boolean bindPermission(HttpServletRequest request);

    Boolean createPermission(AddPermission permission,HttpServletRequest request);

    Boolean deletedPermission(Long permissionId, HttpServletRequest request);

    Permissions getPermission(Long permissionId, HttpServletRequest request);

    public UserDto getUserFromCacheOrDb(HttpServletRequest request);

    Permissions getPermissionByRoleId(String roleId);

    Boolean deletedPermissionForOne(Long permissionId, String roleCode, HttpServletRequest request);

    Permissions getPermissionByRoleIdForMember(String roleId);

}
