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
import java.util.concurrent.atomic.AtomicBoolean;

import static com.kimo.constans.PointConstant.DEFAULT_POINT;

/**
 * @Author Mr.kimo
 * @Description  分数实现
 * @Date
 **/
@Service
public class PointServiceImpl extends ServiceImpl<PointMapper, Point>
    implements PointService {

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private UserMapper userMapper;


    /**
     * @Author: Mr.Kimo
     * @Date: 9:03
     * @return: Boolean
     * @Param: userId pointNumber
     * @Description: 更新分数
     */
    @Override
    public Boolean updatePoint(Long userId, Long pointNumber) {
        // 验证用户ID和积分分数是否有效
        ThrowUtils.throwIf(userId <= 0 || pointNumber <= 0, ErrorCode.NOT_FOUND_ERROR);

        // 查询数据库并检查是否存在
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Point point = pointMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(point == null, ErrorCode.NOT_FOUND_ERROR);

        // 检查用户积分是否足够
        if (point.getPoint() < pointNumber) {
            throw new BusinessException(ErrorCode.POINT_IS_NOT, "Insufficient points: current points are " + point.getPoint());
        }

        // 扣减积分
        point.setPoint(point.getPoint() - pointNumber);

        // 更新分数
        int updatedRows = pointMapper.updateById(point);
        return updatedRows == 1;
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 9:04
     * @return: java.lang.Boolean
     * @Param: [java.lang.Long]
     * @Description: 添加分数表
     */
    public Boolean addPoint(Long userId) {
        // 验证用户ID是否有效
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.INVALID_INPUT_ERROR);

        // 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到具有 ID 的用户: " + userId);
        }

        // 创建积分记录
        Point point = new Point();
        point.setEmail(user.getEmail());

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        point.setCreateTime(now);
        point.setUpdateTime(now);

        // 设置用户ID和默认积分
        point.setUserId(userId);
        point.setPoint(DEFAULT_POINT);

        // 插入记录并返回结果
        int insert = pointMapper.insert(point);
        return insert == 1;
    }

}




