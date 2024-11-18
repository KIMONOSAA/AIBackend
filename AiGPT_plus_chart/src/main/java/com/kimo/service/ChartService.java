package com.kimo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.domain.CouZiAdditionalFileMessage;
import com.kimo.domain.CouZiCompletionFileResponse;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.model.dto.chart.ChartDataRequest;
import com.kimo.model.dto.chart.ChartEditRequest;
import com.kimo.model.dto.chart.ChartQueryRequest;
import com.kimo.model.dto.chart.GenChartyByAIRequest;
import com.kimo.model.dto.po.AIMasterData;
import com.kimo.model.dto.po.Chart;
import com.kimo.model.dto.vo.BiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author Mr.kimo
 */
public interface ChartService extends IService<Chart> {

    Long getLoginUser(HttpServletRequest request);

    String getChartDataForCouZiChartAndFileData(GouZiAdditionalMessages chartData,CouZiAdditionalFileMessage fileData,String botId,String user,String userId,String token) throws Exception;

    Boolean deletedChart(HttpServletRequest request);

    Page<Chart> listMyChartByPage(HttpServletRequest request, ChartQueryRequest chartQueryRequest);

    Boolean editChart(HttpServletRequest request, ChartEditRequest chartEditRequest);

    Page<Chart> listChartUserByPage(long current, long size, ChartQueryRequest chartQueryRequest);

    BiResponse genChartByAIRabbitMQ(MultipartFile multipartFile, GenChartyByAIRequest genChartByAI, HttpServletRequest request);

    Map<String,String> getCourseInfoDataForCouZi(MultipartFile file, HttpServletRequest request);

    Map<String,String> getLearnTeachPlanForCouZi(GouZiAdditionalMessages chartData, String courseId, HttpServletRequest request);

    void ensureAndUpdateAiMasterData(Map<String,String> map,String data);

    void ensureAndCreateAiMasterDataForCourse(Map<String,String> map,String data);

    AIMasterData createAndSaveAiMasterDataWithFile(Map<String,String> map, MultipartFile file, String title);

    Map<String, String> fetchCourseInfoForChart(GouZiAdditionalMessages chartData, String botId, String token, String courseIId, HttpServletRequest request);
}
