package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.ucenter.model.po.Point;

/**
 * @Author kimo
 * @Description  分数服务
 * @Date
 * @Param
 * @return
 * @return null
 **/
public interface PointService extends IService<Point> {
    /**
     * @Author kimo
     * @Description  更新分数
     * @Date
     * @Param point userId
     * @return null
     **/
    Boolean updatePoint(Long userId, Long point);


    /**
     * @Author kimo
     * @Description
     * @Date
     * @param userId
     * @return Boolean
     **/
    Boolean addPoint(Long userId);
}



