package com.kimo.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Mr.kimo
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