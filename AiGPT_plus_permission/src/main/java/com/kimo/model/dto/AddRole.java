package com.kimo.model.dto;

import lombok.Data;

/**
 * @author Mr.kimo
 * @title AddRole
 * @date 2024/11/21 19:55
 * @description TODO
 */
@Data
public class AddRole {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色名
     */
    private String rolesName;

}
