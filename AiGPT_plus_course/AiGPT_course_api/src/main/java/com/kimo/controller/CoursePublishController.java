package com.kimo.controller;


import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.exception.ThrowUtils;
import com.kimo.model.dto.CourseAuditRequest;
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


     @PostMapping("/course/audit/success")
     /**
      * @Author: Mr.kimo
      * @Date: 17:44
      * @return: com.kimo.common.BaseResponse<java.lang.Boolean>
      * @Param: [java.lang.Long, java.lang.Long, jakarta.servlet.http.HttpServletRequest]
      * @Description:课程审核通过
      */
     public BaseResponse<Boolean> auditSuccess(@RequestBody CourseAuditRequest courseAuditRequest, HttpServletRequest request){
          Long courseId = courseAuditRequest.getCourseId();
          String suggestion = courseAuditRequest.getSuggestion();
          Long managerId = courseAuditRequest.getManagerId();
          ThrowUtils.throwIf(courseId <= 0, ErrorCode.NOT_FOUND_ERROR);
          ThrowUtils.throwIf(managerId <= 0, ErrorCode.NOT_FOUND_ERROR);
          ThrowUtils.throwIf(suggestion == null, ErrorCode.NOT_FOUND_ERROR);
          Boolean audit = coursePublishService.auditSuccess(courseId,request,managerId,suggestion);
          return ResultUtils.success(audit);
     }

     @PostMapping("/course/audit/Wrong/{courseId}/{managerId}")
     /**
      * @Author: Mr.kimo
      * @Date: 17:44
      * @return: com.kimo.common.BaseResponse<java.lang.Boolean>
      * @Param: [java.lang.Long, java.lang.Long, jakarta.servlet.http.HttpServletRequest]
      * @Description:课程审核不通过
      */
     public BaseResponse<Boolean> auditWrong(@RequestBody CourseAuditRequest courseAuditRequest, HttpServletRequest request){
          Long courseId = courseAuditRequest.getCourseId();
          String suggestion = courseAuditRequest.getSuggestion();
          Long managerId = courseAuditRequest.getManagerId();
          ThrowUtils.throwIf(courseId <= 0, ErrorCode.NOT_FOUND_ERROR);
          ThrowUtils.throwIf(managerId <= 0, ErrorCode.NOT_FOUND_ERROR);
          ThrowUtils.throwIf(suggestion == null, ErrorCode.NOT_FOUND_ERROR);
          Boolean audit = coursePublishService.auditWrong(courseId,request,managerId,suggestion);
          return ResultUtils.success(audit);
     }


     /**
      * @Author: Mr.kimo
      * @Date: 18:05
      * @return:
      * @Param:
      * @Description:课程下线
      */
     @PostMapping("/course/courseOffline/{courseId}/{managerId}")
     public BaseResponse<Boolean> courseOffline(@PathVariable("courseId") Long courseId,@PathVariable("managerId") Long managerId, HttpServletRequest request){
          Boolean isOffLine = coursePublishService.courseOffLine(courseId,managerId,request);

          return ResultUtils.success(isOffLine);
     }





}