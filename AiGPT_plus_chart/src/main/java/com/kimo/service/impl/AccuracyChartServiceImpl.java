package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kimo.constant.SecurityConstants;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.AccuracyChartMapper;
import com.kimo.model.dto.chart.UserDto;
import com.kimo.model.dto.po.AccuracyChart;
import com.kimo.service.AccuracyChartService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mr.kimo
 */
@Service
public class AccuracyChartServiceImpl extends ServiceImpl<AccuracyChartMapper, AccuracyChart>
    implements AccuracyChartService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private AccuracyChartMapper accuracyChartMapper;

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:04
     * @return: com.kimo.model.dto.po.AccuracyChart
     * @Param: [jakarta.servlet.http.HttpServletRequest]
     * @Description: 根据用户获取准确率表
     */
    public AccuracyChart getAccuracyChartById(HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto user = userClient.GobalGetLoginUser(username);

        QueryWrapper<AccuracyChart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        AccuracyChart accuracyChart = accuracyChartMapper.selectOne(queryWrapper);
        return accuracyChart;
    }
}




