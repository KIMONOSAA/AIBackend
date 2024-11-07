package com.kimo.model.dto;

import com.kimo.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
/**
 * @author Mr.kimo
 */
@Data
public class CoursePublishListDto extends PageRequest implements Serializable {


    private Long id;


    private Long browses;

    /**
     * 课程名称
     */
    private String name;
    /**
     * 课程标签
     */
    private String tags;

    /**
     * 课程等级
     */
    private String grade;

    private String label;

}
