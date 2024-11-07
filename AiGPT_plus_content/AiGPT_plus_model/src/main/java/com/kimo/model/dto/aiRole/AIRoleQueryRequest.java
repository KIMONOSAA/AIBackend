package com.kimo.model.dto.aiRole;

import com.kimo.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 * @author kimo

 * 

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AIRoleQueryRequest extends PageRequest implements Serializable {

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 搜索字段
     */
    private String SearchText;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 是否公开
     */
    private Integer status;

    /**
     * 最大长度限制
     */
    private byte[] AIMax_Tokens;


    private static final long serialVersionUID = 1L;
}