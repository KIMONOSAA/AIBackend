package com.kimo.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;
/**
 * @author Mr.kimo
 */
 @Data
 @ToString
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanListDto> teachplans;
    
    //师资信息暂时不加...


}