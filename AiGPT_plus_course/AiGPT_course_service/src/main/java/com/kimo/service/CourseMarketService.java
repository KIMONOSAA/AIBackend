package com.kimo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.po.CourseMarket;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface CourseMarketService extends IService<CourseMarket> {

    Boolean getHotCoursesBaseMarket();
}
