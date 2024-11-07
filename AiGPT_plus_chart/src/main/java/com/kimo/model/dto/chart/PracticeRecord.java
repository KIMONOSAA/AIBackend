package com.kimo.model.dto.chart;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Mr.kimo
 */
@Data
public class PracticeRecord implements Serializable {
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
     * 考试记录表的创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 学生练习总时间
     */
    private LocalDateTime endTime;

    /**
     * 学历
     */
    private String qualifications;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}