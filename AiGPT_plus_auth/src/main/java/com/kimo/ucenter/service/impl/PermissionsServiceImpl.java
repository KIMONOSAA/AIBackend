package com.kimo.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kimo.ucenter.mapper.PermissionsMapper;
import com.kimo.ucenter.mapper.RolesMapper;
import com.kimo.ucenter.model.dto.UserPermissionDto;
import com.kimo.ucenter.model.po.Permissions;
import com.kimo.ucenter.model.po.Roles;
import com.kimo.ucenter.model.po.User;
import com.kimo.ucenter.service.PermissionsService;
import com.kimo.ucenter.service.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author chen
* @description 针对表【permissions(权限)】的数据库操作Service实现
* @createDate 2024-10-31 11:18:07
*/
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions>
    implements PermissionsService {

    @Autowired
    private RolesMapper rolesMapper;

    @Autowired
    private PermissionsMapper permissionsMapper;

    @Override
    public List<Permissions> getUserPermissions(UserPermissionDto user) {
        Roles roles = rolesMapper.selectById(user.getRoleId());
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roles.getId());

        return permissionsMapper.selectList(queryWrapper);
    }
}




