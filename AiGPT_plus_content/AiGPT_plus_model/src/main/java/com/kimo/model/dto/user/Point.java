package com.kimo.model.dto.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 积分表
 * @TableName point
 */

@Data
public class Point implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 积分总量
     */
    private Long point;


    private static final long serialVersionUID = 1L;

}