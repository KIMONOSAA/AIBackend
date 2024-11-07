package com.kimo.controller;


import com.kimo.common.BaseResponse;
import com.kimo.common.ResultUtils;
import com.kimo.model.dto.CourseLearnRecordDto;
import com.kimo.model.dto.CoursePreviewDto;
import com.kimo.service.CourseBaseService;
import com.kimo.service.CoursePublishService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * @author Mr.kimo
 */
@RestController
public class CourseOpenController {

     @Autowired
     private CourseBaseService courseBaseInfoService;

     @Autowired
     private CoursePublishService coursePublishService;

    /**
     * 获取课程的完整预览信息
     *
     * @param courseId 课程Id
     * @return 返回课程预览信息 DTO
     */
    @PostMapping("/course/whole/{courseId}")
    public BaseResponse<CoursePreviewDto> getPreviewInfo(@PathVariable("courseId") Long courseId, CourseLearnRecordDto courseLearnRecordDto, HttpServletRequest request) {
        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId,request,courseLearnRecordDto);
        return ResultUtils.success(coursePreviewInfo);
    }

}