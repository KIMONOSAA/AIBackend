package com.kimo.jobhandler;

import com.kimo.common.ErrorCode;
import com.kimo.exception.ThrowUtils;
import com.kimo.model.po.CourseBase;
import com.kimo.model.po.CourseMarket;
import com.kimo.service.CourseBaseService;
import com.kimo.service.CourseMarketService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * @author Mr.kimo
 */
@Slf4j
@Component
public class CachePreheatJob {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private CourseMarketService courseMarketService;

    @XxlJob("cachePreheatJobHandler")
    public void cachePreheat() {

        //分页获取课程信息并且批量插入redis中中,使用redis管道
        Boolean hotCoursesBase = courseBaseService.getHotCoursesBase();
        ThrowUtils.throwIf(!hotCoursesBase, ErrorCode.SYSTEM_ERROR);

        //分页获取营销信息并且批量插入redis中,使用redis的管道
        Boolean hotCoursesBaseMarket = courseMarketService.getHotCoursesBaseMarket();
        ThrowUtils.throwIf(!hotCoursesBaseMarket, ErrorCode.SYSTEM_ERROR);

    }
}
