package com.kimo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
/**
 * @author Mr.kimo
 */
@TableName(value ="course_publish")
@Data
public class CoursePublish implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 管理员ID
     */
    private Long managerId;

    /**
     * 管理员名字
     */
    private String manager;


    /**
     * 课程名称
     */
    private String name;

    /**
     * 适用人群
     */
    private String users;

    /**
     * 标签
     */
    private String tags;


    private String label;

    /**
     * 创建人
     */
    private String username;


    /**
     * 课程等级
     */
    private String grade;

    /**
     * 教育模式
     */
    private String teachmode;

    /**
     * 课程图片
     */
    private String pic;

    /**
     * 课程介绍
     */
    private String description;

    /**
     * 课程营销信息，json格式
     */
    private String market;

    /**
     * 所有课程计划，json格式
     */
    private String teachplan;

    /**
     * 教师信息，json格式
     */
    private String teachers;

    /**
     * 发布时间
     */
    private Date createDate;

    /**
     * 上架时间
     */
    private Date onlineDate;

    /**
     * 下架时间
     */
    private Date offlineDate;

    /**
     * 发布状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 收费规则，对应数据字典--203
     */
    private String charge;

    /**
     * 现价
     */
    private Double memberPrice;


    /**
     * 课程有效期天数
     */
    private Integer validDays;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}