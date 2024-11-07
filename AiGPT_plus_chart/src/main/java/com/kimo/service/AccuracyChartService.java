package com.kimo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.po.AccuracyChart;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mr.kimo
 */
public interface AccuracyChartService extends IService<AccuracyChart> {

    AccuracyChart getAccuracyChartById(HttpServletRequest request);
}
