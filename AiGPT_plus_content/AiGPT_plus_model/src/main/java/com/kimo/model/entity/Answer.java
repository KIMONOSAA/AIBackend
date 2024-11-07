package com.kimo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户答题表
 * @TableName answer
 */
@TableName(value ="answer")
@Data
public class Answer implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 用户选的科目
     */
    private String userSubjects;

    /**
     * 用户答对的题数
     */
    private Long userRight;

    /**
     * 用户答错的题数
     */
    private Long userWrong;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 用户总百分比
     */
    private Integer userResult;

    /**
     * ai的建议与评价
     */
    private String aiBody;

    /**
     * wait-未考,running-已考但未提交.succeed-表示已提交,failed-网络错误
     */
    private String status;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}