package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.ucenter.model.po.Point;

/**
* @author chen
* @description 针对表【point(积分表)】的数据库操作Service
* @createDate 2024-10-25 00:16:42
*/
public interface PointService extends IService<Point> {

    Boolean updatePoint(Long userId, Long point);
}



