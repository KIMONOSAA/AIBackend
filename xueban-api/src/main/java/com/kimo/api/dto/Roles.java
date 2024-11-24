package com.kimo.api.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * 角色
 * @TableName roles
 */
@Data
public class Roles implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色名
     */
    private String rolesName;


    private static final long serialVersionUID = 1L;

}