package com.kimo.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.api.dto.UserDto;
import com.kimo.common.BaseResponse;

import com.kimo.model.dto.*;
import com.kimo.model.po.CourseBase;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface CourseBaseService extends IService<CourseBase> {

    Wrapper<CourseBase> getQueryWrapper(Long id,QueryCourseParamsDto queryCourseParams, HttpServletRequest request);

    Wrapper<CourseBase> getQueryWrapperList(CoursePublishListDto coursePublishListDto, HttpServletRequest request);

    CourseBaseInfoDto createCourseBase(AddCourseDto addCourseDto, HttpServletRequest request);

    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    UserDto getUserDtoForRedisOrLock(HttpServletRequest request, String type);

    CourseBaseInfoDto updateCourseBase(EditCourseDto editCourseDto, HttpServletRequest request);

    Boolean getHotCoursesBase();

    public void ensuperAdminOrAdmin(String code,String expected);

    public String getRoleForPermission(UserDto userDtoForRedisOrLock);

    Boolean deletedTeachplanOrMedia(Long teachId,Long courseId, HttpServletRequest request);

    CourseBase getCourseBaseInfoByRedis(Long courseId);

    CourseBase getCourseBaseInfoN(Long courseId);

    List<TeachplanListDto> findTeachplanTreeRedis(Long courseId);

    Boolean deletedCourseBase(Long courseId, HttpServletRequest request);
}
