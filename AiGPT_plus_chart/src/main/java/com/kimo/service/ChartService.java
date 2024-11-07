package com.kimo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.model.dto.chart.ChartDataRequest;
import com.kimo.model.dto.chart.ChartEditRequest;
import com.kimo.model.dto.chart.ChartQueryRequest;
import com.kimo.model.dto.chart.GenChartyByAIRequest;
import com.kimo.model.dto.po.Chart;
import com.kimo.model.dto.vo.BiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
/**
 * @author Mr.kimo
 */
public interface ChartService extends IService<Chart> {

    Long getLoginUser(HttpServletRequest request);

//    String getChartData(ChartDataRequest chartData) throws Exception;

     String getChartDataForCouZiChart(GouZiAdditionalMessages chartData,String botId,String user,String token) throws Exception;

//    String getChartData(String chartData) throws Exception;

    Boolean deletedChart(HttpServletRequest request);

    Page<Chart> listMyChartByPage(HttpServletRequest request, ChartQueryRequest chartQueryRequest);

    Boolean editChart(HttpServletRequest request, ChartEditRequest chartEditRequest);

    Page<Chart> listChartUserByPage(long current, long size, ChartQueryRequest chartQueryRequest);

    BiResponse genChartByAIRabbitMQ(MultipartFile multipartFile, GenChartyByAIRequest genChartByAI, HttpServletRequest request);
}
