package com.kimo.model.dto.chart;

import com.kimo.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * @author Mr.kimo
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PracticeRecordRequest  extends PageRequest implements Serializable{


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

    private static final long serialVersionUID = 1L;

}
