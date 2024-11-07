package com.kimo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.mapper.PointMapper;
import com.kimo.model.entity.Point;
import com.kimo.service.PointService;
import org.springframework.stereotype.Service;

/**
 * @author Mr.kimo
 */
@Service
public class PointServiceImpl extends ServiceImpl<PointMapper, Point>
    implements PointService{

}




