package com.kimo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 学生练习答题数据表
 * @TableName practice_answer
 */
@TableName(value ="practice_answer")
@Data
public class PracticeAnswer implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目数据库表id
     */
    private Long questionId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 练习记录id
     */
    private Long practiceId;

    /**
     * 用户结果答案
     */
    private String userResult;

    /**
     * 状态用户答题结果是错误的 用户答题结果正确
     */
    private Boolean answerStatus;

    /**
     * 创建表时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 学历
     */
    private String qualifications;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}