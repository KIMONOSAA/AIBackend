package com.kimo.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
/**
 * @author Mr.kimo
 */
@Data
public class TeachplanDto implements Serializable {

    private Long id;

    /**
     * 课程计划名称
     */
    private String pname;

    /**
     * 课程类型:1视频、2文档
     */
    private String mediaType;
    /**
     * 章节及课程时介绍
     */
    private String description;

    /**
     * 时长，单位时:分:秒
     */
    private String timelength;
    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 课程发布标识
     */
    private Long coursePubId;

    /**
     * 状态（1正常  0删除）
     */
    private Integer status;

    /**
     * 排序字段
     */
    private Integer orderby;

    private String label;


    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 修改时间
     */
    private LocalDateTime changeDate;

}
