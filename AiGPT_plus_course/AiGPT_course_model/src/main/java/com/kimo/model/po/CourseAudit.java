package com.kimo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 审核表
 * @TableName course_audit
 */
@TableName(value ="course_audit")
@Data
public class CourseAudit implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 管理员ID
     */
    private Long managerId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 管理员名字
     */
    private String manager;

    /**
     * 课程审核不通过建议
     */
    private String suggestion;

    /**
     * 审核状态表
     */
    private String status;

    /**
     *
     */
    private LocalDateTime createTime;

    /**
     *
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}