package com.kimo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.CourseLearnRecordDto;
import com.kimo.model.dto.CoursePreviewDto;
import com.kimo.model.po.CoursePublish;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.kimo
 */
public interface CoursePublishService extends IService<CoursePublish> {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return CoursePreviewDto
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId, HttpServletRequest request, CourseLearnRecordDto courseLearnRecordDto);

    /**
     * @description 提交审核
     * @param courseId  课程id
     * @return void
     */
    public void commitAudit(Long courseId, HttpServletRequest request);

    /**
     * @description 课程发布接口
     * @param courseId 课程id
     * @return void
     */
    public void publish(HttpServletRequest request,Long courseId);


    CoursePublish getCoursePublish(Long courseId);
}
