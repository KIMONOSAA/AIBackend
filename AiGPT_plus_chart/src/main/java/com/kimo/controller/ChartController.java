package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.annotation.AuthCheck;
import com.kimo.common.*;
import com.kimo.amqp.ChartProducer;
import com.kimo.constant.UserConstant;
import com.kimo.domain.CouZiAdditionalFileMessage;
import com.kimo.domain.CouZiCompletionFileResponse;
import com.kimo.domain.CouZiMessageAndMasterRequest;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.mapper.ChartMapper;
import com.kimo.model.dto.chart.*;
import com.kimo.model.dto.po.AIMasterData;
import com.kimo.model.dto.po.AccuracyChart;
import com.kimo.model.dto.po.Chart;
import com.kimo.model.dto.po.PracticeRecordPro;
import com.kimo.model.dto.vo.BiResponse;
import com.kimo.service.AIMessageSessionService;
import com.kimo.service.AccuracyChartService;
import com.kimo.service.ChartService;

import com.kimo.service.PracticeRecordProService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI设定角色图表BI
 *
 * @author  kimo
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Autowired
    private ChartMapper chartMapper;

    @Autowired
    private AccuracyChartService accuracyChartService;

    @Autowired
    private PracticeRecordProService practiceRecordProService;


    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        Long userId = chartService.getLoginUser(request);

        chart.setUserId(userId);
        int insert = chartMapper.insert(chart);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean b = chartService.deletedChart(request);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/admin/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartAdminByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();

        Page<Chart> chartPage = chartService.listChartUserByPage(current,size,chartQueryRequest);
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/chart/page")
    public BaseResponse<Page<Chart>> listChartUserByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.listChartUserByPage(current,size,chartQueryRequest);

        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Chart> chartPage = chartService.listMyChartByPage(request,chartQueryRequest);

        return ResultUtils.success(chartPage);
    }

    // endregion



    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        Boolean result = chartService.editChart(request,chartEditRequest);
        return ResultUtils.success(result);
    }

    /**
     * 智能分析异步
     *
     * @param multipartFile
     * @param genChartByAI
     * @param request
     * @return
     */
    @PostMapping("/gen/rabbit/async")
    public BaseResponse<BiResponse> genChartByAIRabbitMQ(@RequestPart("file") MultipartFile multipartFile,GenChartyByAIRequest genChartByAI, HttpServletRequest request) {

        BiResponse biResponse = chartService.genChartByAIRabbitMQ(multipartFile,genChartByAI,request);

        return ResultUtils.success(biResponse);
    }


//    /**
//     * 智能分析异步
//     * @return
//     */
//    @PostMapping("/gen/chatdata/async")
//    public String genChartData(@RequestBody ChartDataRequest chartData)  {
//
//        String biResponse = null;
//        try {
//            biResponse = chartService.getChartData(chartData);
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//
//        return biResponse;
//    }

    /**
     * 使用扣子可视化视图
     * @return
     */
    @PostMapping("/gen/couzi/async")
    public BaseResponse<String> getChartDataForCouZi(@RequestBody GouZiAdditionalMessages chartData)  {

        String botId = "7432966743104520192";
        String user = "user";
        String token = "pat_7gwklsLnL5KGDMGecF6IuLazLWBNDqwyELV7nGUGrD215fi1D2yjWSKkzSSiVijO";
        String biResponse = null;
        try {
            biResponse = chartService.getChartDataForCouZiChartAndFileData(chartData,null,botId,user,token);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return ResultUtils.success(biResponse);
    }

    /**
     * 智能辅导-总结课程
     *
     */
    @PostMapping("/gen/couzi/course/{courseId}")
    public BaseResponse<String> getCourseInfoDataForCouZi(@RequestBody GouZiAdditionalMessages chartData,@PathVariable("courseId") Long courseId,HttpServletRequest request)  {
        String courseIId = String.valueOf(courseId);
        String title = "总结课程";
        String botId = "7436587258616348712";
//        String user = "user";
        String token = "pat_qlj1MPjOw6Z9BsaSCqHS0gLKPQoya6XWIR3poLgG9MZbvtsQXsXLLUJPkIJ34GHX";
        ThrowUtils.throwIf(courseIId.isBlank(), ErrorCode.ADD_DATABASE_ERROR);
        Map<String, String> courseInfoDataForCouZi = null;
        try {
            courseInfoDataForCouZi = chartService.getCourseInfoDataForCouZi(chartData,botId,token, courseIId, request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        chartService.IsAiMessagesession(courseInfoDataForCouZi,title);
        return ResultUtils.success(courseInfoDataForCouZi.get("aiData"));
    }


    /**
     * 智能辅导-图片AI上传
     *
     */
    @PostMapping("/gen/couzi/pic")
    public BaseResponse<Map<String, String>> getPicResultForCouZi(@RequestPart("file") MultipartFile multipartFile,HttpServletRequest request)  {

        Map<String, String> courseInfoDataForCouZi = null;
        try {
            courseInfoDataForCouZi = chartService.getCourseInfoDataForCouZi(multipartFile, request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        AIMasterData aiMasterData = chartService.IsAiMessagesession(courseInfoDataForCouZi, multipartFile, "图片识别");
        courseInfoDataForCouZi.put("aiMaster",aiMasterData.getId().toString());
        courseInfoDataForCouZi.put("aisession",aiMasterData.getAiMessageSessionId().toString());
        return ResultUtils.success(courseInfoDataForCouZi);
    }

    /**
     * 智能辅导-图片AI生成答案与分析
     */
    @PostMapping("/gen/couzi/pic/data")
    public BaseResponse<String> getPicResultForCouZiForData(@RequestBody CouZiMessageAndMasterRequest gouZiAdditionalMessagesAndMaster, HttpServletRequest request)  {
        String courseIId = "65";
        String botId = "7436728231417544739";
//        String user = "user";
        String token = "pat_M6W3gFhKK9qwkj6IceAhBS29nSKarYfoWd1C6iDtUOD0Knv2nYXoMxs72TNrJ55Y";
        GouZiAdditionalMessages gouZiAdditionalMessages = new GouZiAdditionalMessages();
        gouZiAdditionalMessages.setRole(gouZiAdditionalMessagesAndMaster.getRole());
        gouZiAdditionalMessages.setContent_type(gouZiAdditionalMessagesAndMaster.getContent_type());
        gouZiAdditionalMessages.setContent(gouZiAdditionalMessagesAndMaster.getContent());
        Long masterId = gouZiAdditionalMessagesAndMaster.getMasterId();
        Long sessionId = gouZiAdditionalMessagesAndMaster.getSessionId();
        Map<String, String> courseInfoDataForCouZi = null;
        try {
            courseInfoDataForCouZi = chartService.getCourseInfoDataForCouZi(gouZiAdditionalMessages,botId,token, courseIId, request);
            courseInfoDataForCouZi.put("masterId",masterId.toString());
            courseInfoDataForCouZi.put("sessionId",sessionId.toString());
            chartService.IsAiMessagesession(courseInfoDataForCouZi,"图片识别");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(courseInfoDataForCouZi.get("aiData"));

    }





    /**
     * 智能辅导-学习计划路线
     *
     */
    @PostMapping("/gen/couzi/learn/{courseId}")
    public BaseResponse<String> getLearnTeachPlanForCouZi(@RequestBody GouZiAdditionalMessages chartData,@PathVariable("courseId") Long courseId,HttpServletRequest request)  {
        String courseIId = String.valueOf(courseId);
        String title = "学习计划路线";
        Map<String, String> courseInfoDataForCouZi = null;
        try {
            courseInfoDataForCouZi = chartService.getLearnTeachPlanForCouZi(chartData,courseIId,request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        chartService.IsAiMessagesession(courseInfoDataForCouZi,title);
        return ResultUtils.success(courseInfoDataForCouZi.get("aiData"));
    }


    /**
     *
     *
     * 获取可视化视图
     * @return
     */
    @GetMapping("/Bi")
    public BaseResponse<AccuracyChart> getAccuracyChartById(HttpServletRequest request) {

        AccuracyChart accuracyChart = accuracyChartService.getAccuracyChartById(request);
        return ResultUtils.success(accuracyChart);
    }


    /**
     * 分页获取当前用户创建的资源列表
     *
     *
     * @param request
     * @return
     */
    @PostMapping("/my/list/practice/page")
    public BaseResponse<Page<PracticeRecordPro>> listMyPracticeByPage(@RequestBody PracticeRecordRequest pageRequest,
                                                                      HttpServletRequest request) {
        if (pageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<PracticeRecordPro> chartPage = practiceRecordProService.listMyChartByPage(request,pageRequest);

        return ResultUtils.success(chartPage);
    }




}
