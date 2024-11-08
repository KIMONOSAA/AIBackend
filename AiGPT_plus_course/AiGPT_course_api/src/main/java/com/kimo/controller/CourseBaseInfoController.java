package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import com.kimo.annotation.PermissionMethod;
import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.constant.SecurityConstants;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;

import com.kimo.model.dto.*;
import com.kimo.model.po.CourseBase;
import com.kimo.model.po.CourseLearnRecord;
import com.kimo.service.CourseBaseService;
import com.kimo.service.CourseLearnRecordService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description 课程信息编辑接口
 * @author Mr.Kimo
 * @version 1.0
 */

@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseService courseBaseInfoService;


    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private CourseBaseService courseBaseService;


    @Autowired
    private CourseLearnRecordService courseLearnRecordService;


    /**
     * 列出当前用户的所有课程
     * @param queryCourseParams
     * @param request
     * @return
     */
    @PostMapping("/course/list")
    @PermissionMethod(permission = "course_manager_course_all")
    public BaseResponse<Page<CourseBase>> list(@RequestBody(required = false) QueryCourseParamsDto queryCourseParams, HttpServletRequest request){

        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(userDto.getId() <= 0,ErrorCode.FORBIDDEN_ERROR);
        long current = queryCourseParams.getCurrent();
        long size = queryCourseParams.getPageSize();
        long id = userDto.getId();
        Page<CourseBase> courseBasePage = courseBaseService.page(new Page<>(current, size),
                courseBaseService.getQueryWrapper(id,queryCourseParams,request));
        return ResultUtils.success(courseBasePage);
    }


    /**
     * 列出当前用户的所有课程
     * @param queryCourseParams
     * @param request
     * @return
     */
    @PostMapping("/course/list/record")
    @PermissionMethod(permission = "course_manager_course_all")
    public BaseResponse<Page<CourseLearnRecord>> listCourseRecord(@RequestBody  QueryCourseParamsDto queryCourseParams, HttpServletRequest request){

        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(userDto.getId() <= 0,ErrorCode.FORBIDDEN_ERROR);
        long current = queryCourseParams.getCurrent();
        long size = queryCourseParams.getPageSize();
        long id = userDto.getId();
        Page<CourseLearnRecord> courseBasePage = courseLearnRecordService.page(new Page<>(current, size),
                courseLearnRecordService.getQueryWrapper(id,queryCourseParams,request));
        return ResultUtils.success(courseBasePage);
    }



    /**
     * 创建课程
     * @param addCourseDto
     * @return
     */
    @PostMapping("/add/course")
    @PermissionMethod(permission = "course_manager_course_all")
    public BaseResponse<CourseBaseInfoDto> createCourseBase(@RequestBody AddCourseDto addCourseDto,HttpServletRequest request){

        return ResultUtils.success(courseBaseInfoService.createCourseBase(addCourseDto,request));
    }


    /**
     * 获取课程信息
     * @param courseId
     * @return
     */
    @GetMapping("/course/{courseId}")
    public BaseResponse<CourseBase> getCourseBaseById(@PathVariable("courseId") Long courseId){
        CourseBase courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfoN(courseId);

        return ResultUtils.success(courseBaseInfoDto);
    }


    /**
     * 获取课程信息
     * @param courseId
     * @return
     */
    @GetMapping("/course/redis/{courseId}")
    public BaseResponse<CourseBase> getCourseBaseByRedis(@PathVariable("courseId") Long courseId){
        CourseBase courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfoByRedis(courseId);

        return ResultUtils.success(courseBaseInfoDto);
    }

    /**
     * 修改课程
     * @param editCourseDto
     * @return
     */
    @PutMapping("/update/course")
    @PermissionMethod(permission = "course_manager_course_all")
    public BaseResponse<CourseBaseInfoDto> modifyCourseBase(@RequestBody EditCourseDto editCourseDto,HttpServletRequest request){

        return ResultUtils.success(courseBaseInfoService.updateCourseBase(editCourseDto,request));
    }

    /**
     * 分页列出所有课程信息
     */
    @PostMapping("/list/course/data")
    public BaseResponse<Page<CourseBase>> listAiMasterDataByPage(@RequestBody CoursePublishListDto coursePublishListDto, HttpServletRequest request) {
        long current = coursePublishListDto.getCurrent();
        long size = coursePublishListDto.getPageSize();


        Page<CourseBase> aiRolesPage = courseBaseService.page(new Page<>(current, size),
                courseBaseService.getQueryWrapperList(coursePublishListDto,request));
        return ResultUtils.success(aiRolesPage);
    }
}