package com.kimo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.po.CourseMarket;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface CourseMarketMapper extends BaseMapper<CourseMarket> {
    int getCourseMarketCount();

    List<CourseMarket> getCourseMarketListByPage(@Param("page") Integer page, @Param("size") Integer size);
}




