package com.kimo.controller;


import com.kimo.common.BaseResponse;
import com.kimo.common.ResultUtils;
import com.kimo.model.dto.CourseLearnRecordDto;
import com.kimo.model.dto.CoursePreviewDto;
import com.kimo.model.po.CoursePublish;
import com.kimo.service.CoursePublishService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.kimo
 */
@RestController
public class CoursePublishController {


     @Autowired
     CoursePublishService coursePublishService;

     /**
      * 获取课程预览信息
      *
      * @param courseId 课程Id
      * @return 返回课程预览信息的视图模型
      */
     @GetMapping("/coursepreview/{courseId}")
     public BaseResponse<CoursePreviewDto> preview(@PathVariable("courseId") Long courseId, CourseLearnRecordDto courseLearnRecordDto, HttpServletRequest request){

          //获取课程预览信息
          CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId,request,courseLearnRecordDto);


          return ResultUtils.success(coursePreviewInfo);
     }

     /**
      * 获取课程发布信息
      *
      * @param courseId 课程Id
      * @return 返回课程发布信息对象
      */
     @ResponseBody
     @GetMapping("/r/coursepublish/{courseId}")
     public BaseResponse<CoursePublish> getCoursepublish(@PathVariable("courseId") Long courseId) {
          return ResultUtils.success(coursePublishService.getCoursePublish(courseId));
     }

     /**
      * 提交课程审核
      *
      * @param courseId 课程Id
      */
     @ResponseBody
     @PostMapping("/courseaudit/commit/{courseId}")
     public void commitAudit(@PathVariable("courseId") Long courseId, HttpServletRequest request){
          coursePublishService.commitAudit(courseId,request);
     }

     /**
      * 课程发布
      *
      * @param courseId 课程Id
      */
     @ResponseBody
     @PostMapping ("/coursepublish/{courseId}")
     public void coursepublish(@PathVariable("courseId") Long courseId,HttpServletRequest request){
          coursePublishService.publish(request,courseId);
     }



}