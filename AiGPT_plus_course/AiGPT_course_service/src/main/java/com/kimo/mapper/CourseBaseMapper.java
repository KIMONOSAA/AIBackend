package com.kimo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.po.CourseBase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface CourseBaseMapper extends BaseMapper<CourseBase> {

    int getCourseCount();

    List<CourseBase> getCourseListByPage(@Param("page") Integer page, @Param("size") Integer size);
}




