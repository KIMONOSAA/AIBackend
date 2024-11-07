package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.ucenter.model.dto.UserPermissionDto;
import com.kimo.ucenter.model.po.Permissions;
import com.kimo.ucenter.model.po.User;

import java.util.List;

/**
* @author chen
* @description 针对表【permissions(权限)】的数据库操作Service
* @createDate 2024-10-31 11:18:07
*/
public interface PermissionsService extends IService<Permissions> {
    public List<Permissions> getUserPermissions(UserPermissionDto user);
}
