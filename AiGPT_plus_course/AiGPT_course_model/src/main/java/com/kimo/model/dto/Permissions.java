package com.kimo.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Mr.kimo
 */
@Data
public class Permissions implements Serializable {
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