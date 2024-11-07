package com.kimo.model.dto;


import com.kimo.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Mr.kimo
 */
 @Data
public class QueryCourseParamsDto extends PageRequest implements Serializable {

    //审核状态
   private String auditStatus;
   //课程名称
   private String courseName;
    //发布状态
   private String publishStatus;

}