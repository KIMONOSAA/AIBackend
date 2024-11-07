package com.kimo.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.common.PageRequest;
import com.kimo.model.dto.chart.PracticeRecordRequest;
import com.kimo.model.dto.po.PracticeRecordPro;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mr.kimo
 */
public interface PracticeRecordProService extends IService<PracticeRecordPro> {

    Page<PracticeRecordPro> listMyChartByPage(HttpServletRequest request, PracticeRecordRequest pageRequest);
}
