package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.ucenter.model.dto.UserPermissionDto;
import com.kimo.ucenter.model.po.Permissions;


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
}
