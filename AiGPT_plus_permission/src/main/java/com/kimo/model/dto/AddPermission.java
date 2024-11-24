package com.kimo.model.dto;

import lombok.Data;

/**
 * @author Mr.kimo
 * @title AddPermission
 * @date 2024/11/18 22:40
 * @description TODO
 */
@Data
public class AddPermission {
    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 权限代码
     */
    private String code;

    /**
     * 权限代码名
     */
    private String permissionName;
}
