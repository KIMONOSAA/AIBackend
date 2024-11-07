package com.kimo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ai角色设定表
 * @TableName airole
 */
@TableName(value ="airole")
@Data
public class AIRole implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 用户Id
     */
    private Long userId;

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
     * 1-待审核，2-审核中，3-拒绝
     */
    private Integer AIRoleReview;

    /**
     * 是否公开
     */
    private Integer status;

    /**
     * 最大长度限制
     */
    private Integer AIMax_Tokens;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}