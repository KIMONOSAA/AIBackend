package com.kimo.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.chart.AIMasterDataQueryRequest;
import com.kimo.model.dto.po.AIMasterData;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mr.kimo
 */
public interface AIMasterDataService extends IService<AIMasterData> {

//    boolean validAiMasterData(AIMasterData aiMasterData, HttpServletRequest request) throws Exception;

    String getChartDataForCouZiChart(AIMasterData chartData,HttpServletRequest request, String botId, String user,String token) throws Exception;

    Wrapper<AIMasterData> getQueryWrapper(AIMasterDataQueryRequest aiMasterDataQueryRequest, HttpServletRequest request);
}
