package com.kimo.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.ucenter.mapper.PointMapper;
import com.kimo.ucenter.mapper.UserMapper;
import com.kimo.ucenter.model.po.Point;
import com.kimo.ucenter.model.po.User;
import com.kimo.ucenter.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author chen
* @description 针对表【point(积分表)】的数据库操作Service实现
* @createDate 2024-10-25 00:16:42
*/
@Service
public class PointServiceImpl extends ServiceImpl<PointMapper, Point>
    implements PointService {

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private UserMapper userMapper;


    @Override
    public Boolean updatePoint(Long userId, Long pointNumber) {
        ThrowUtils.throwIf(userId <= 0, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(pointNumber <= 0 , ErrorCode.NOT_FOUND_ERROR);
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Point point = pointMapper.selectOne(queryWrapper);
        if (point.getPoint() < pointNumber){
            throw new BusinessException(ErrorCode.POINT_IS_NOT);
        }
        point.setPoint(point.getPoint()-pointNumber);
        int i = pointMapper.updateById(point);
        return i == 1;
    }

    @Override
    public Boolean addPoint(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Point point = new Point();
        point.setEmail(user.getEmail());
        point.setCreateTime(LocalDateTime.now());
        point.setUserId(userId);
        point.setUpdateTime(LocalDateTime.now());
        point.setPoint(100L);
        int insert = pointMapper.insert(point);
        return insert == 1;

    }
}




