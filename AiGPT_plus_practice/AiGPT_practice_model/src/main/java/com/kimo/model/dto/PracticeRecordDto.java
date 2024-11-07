package com.kimo.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 练习记录表
 * @TableName practice_record
 */
@Data
public class PracticeRecordDto implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 课程id
     */
    private Long courseId;


    private Long teachplanId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     *  (答对+1)   默认为0
     */
    private Integer userRight;

    /**
     *  (答错+1)   默认为0
     */
    private Integer userError;

    /**
     * 未考完，考完
     */
    private String practiceStatus;

    /**
     * (ai的评估评价)
     */
    private String airesult;

    /**
     * （科目）
     */
    private String subjects;

    /**
     * （记录练习的分数）
     */
    private Double practicePoint;

    /**
     * 学历
     */
    private String qualifications;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}