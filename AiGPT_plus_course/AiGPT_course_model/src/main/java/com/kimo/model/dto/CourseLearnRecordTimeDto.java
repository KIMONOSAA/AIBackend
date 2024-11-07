package com.kimo.model.dto;

import lombok.Data;
/**
 * @author Mr.kimo
 */
@Data
public class CourseLearnRecordTimeDto {


    private Long courseId;

    private String courseName;

    private CourseTeachPlanRecord courseTeachPlanRecord;
}
