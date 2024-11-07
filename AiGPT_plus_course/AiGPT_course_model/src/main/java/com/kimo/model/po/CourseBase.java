package com.kimo.model.po;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Mr.kimo
 */
@TableName(value ="course_base")
@Data
public class CourseBase implements Serializable {
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
     * 管理员名字
     */
    private String manager;


    private Long browses;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 适用人群
     */
    private String users;

    /**
     * 课程标签
     */
    private String tags;


    /**
     * 课程等级
     */
    private String grade;

    /**
     * 教育模式(common普通，record 录播，live直播等）
     */
    private String teachmode;

    /**
     * 课程介绍
     */
    private String description;

    /**
     * 课程图片
     */
    private String pic;


    /**
     * 课程是否收费
     */
    private String charge;


    private String label;

    /**
     * 默认会员价
     */
    private Long memberPrice;


    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 修改时间
     */
    private LocalDateTime changeDate;


    /**
     * 审核状态 0-表示未审核 1-表示已审核
     */
    private String auditStatus;

    /**
     * 课程发布状态 0-未发布  1-已发布 2-下线
     */
    private String status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}