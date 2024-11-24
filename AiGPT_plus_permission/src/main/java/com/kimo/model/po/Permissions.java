package com.kimo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import lombok.Data;

/**
 * 权限
 * @TableName permissions
 */
@TableName(value ="permissions")
@Data
public class Permissions implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 权限代码
     */
    private String code;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}