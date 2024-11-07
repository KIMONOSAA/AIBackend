package com.kimo.model.dto.aiRole;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author kimo

 * 

 */
@Data
public class AIRoleUpdateRequest implements Serializable {


    /**
     * id
     */
    private Long id;

    /**
     * ai角色
     */
    private String AIRole;

    /**
     * ai设定描述
     */
    private String AIDescription;


    /**
     * 是否公开
     */
    private Integer status;

    /**
     * 最大长度限制
     */
    private Integer AIMax_Tokens;

    private static final long serialVersionUID = 1L;
}