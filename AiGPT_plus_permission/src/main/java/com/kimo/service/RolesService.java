package com.kimo.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import com.kimo.model.dto.AddRole;
import com.kimo.model.dto.UserRoleQueryRequest;
import com.kimo.model.po.Roles;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author: Mr.kimo
 * @Date: 10:48
 * @return:
 * @Param:
 * @Description:
 */
public interface RolesService extends IService<Roles> {

    Boolean deletedRole(Long roleId,HttpServletRequest request);

    Boolean createdRole(AddRole role, HttpServletRequest request);

    Wrapper<Roles> getQueryWrapper(UserRoleQueryRequest userPermissionQueryRequest,HttpServletRequest request);

    Roles getRolesById(String roleId,HttpServletRequest request);

    Boolean getUserIsAdmin(HttpServletRequest request);

}
