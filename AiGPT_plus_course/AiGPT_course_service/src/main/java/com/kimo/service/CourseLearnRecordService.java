package com.kimo.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.CourseLearnRecordTimeDto;
import com.kimo.model.dto.QueryCourseParamsDto;
import com.kimo.model.po.CourseLearnRecord;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mr.kimo
 */
public interface CourseLearnRecordService extends IService<CourseLearnRecord> {

    Boolean updateCourseRecord(CourseLearnRecordTimeDto courseLearnRecordTimeDto, HttpServletRequest request);

    Wrapper<CourseLearnRecord> getQueryWrapper(QueryCourseParamsDto queryCourseParams, HttpServletRequest request);

    CourseLearnRecord getCourseMyLearnByCourseId(HttpServletRequest request, Long courseId);
}
