package com.kimo.controller;

import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.model.dto.*;
import com.kimo.service.CourseLearnRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
/**
 * @author Mr.kimo
 */
@RestController
public class CourseLearnRecordController {


    @Autowired
    private CourseLearnRecordService courseLearnRecordDto;

    /**
     * 更新课程学习记录
     */
    @PostMapping("/course/record")
    public BaseResponse updateCourseRecord(@RequestBody CourseLearnRecordTimeDto courseLearnRecordTimeDto, HttpServletRequest request) {
        //获取课程预览信息
        Boolean isSave = courseLearnRecordDto.updateCourseRecord(courseLearnRecordTimeDto,request);
        if(isSave){
            return ResultUtils.success(isSave);
        }
        return ResultUtils.error(ErrorCode.ADD_COURSE_INFO_ERROR);

    }

}
