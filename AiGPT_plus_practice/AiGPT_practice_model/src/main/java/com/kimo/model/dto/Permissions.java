package com.kimo.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限
 * @TableName permissions
 */
@Data
public class Permissions implements Serializable {
    /**
     * 
     */
    private Integer id;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}