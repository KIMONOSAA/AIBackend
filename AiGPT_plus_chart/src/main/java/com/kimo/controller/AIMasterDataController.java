package com.kimo.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.common.BaseResponse;
import com.kimo.common.DeleteRequest;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.mapper.AIMasterdataMapper;

import com.kimo.model.dto.chart.AIMasterDataAddRequest;
import com.kimo.model.dto.chart.AIMasterDataQueryRequest;
import com.kimo.model.dto.po.AIMasterData;
import com.kimo.model.dto.po.AIMessageSession;
import com.kimo.service.AIMasterDataService;
import com.kimo.service.AIMessageSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 *
 * 主AI对话信息
 * @author  kimo
 */
@RestController
@RequestMapping("/content")
@Slf4j
public class AIMasterDataController {

    @Autowired
    private AIMasterDataService aiMasterDataService;

    @Autowired
    private AIMessageSessionService aiMessageSessionService;

    @Autowired
    private AIMasterdataMapper aiMasterdataMapper;


//    @Autowired
//    private Executor executor;

    // region 增删改查

    /**
     * AI问答
     *
     * @param aiMasterDataAddRequest
     * @param
     * @return
     */
    @PostMapping("/add/ai/master/data")
    public BaseResponse<AIMasterData> addAiMasterData(@RequestBody AIMasterDataAddRequest aiMasterDataAddRequest, HttpServletRequest request) throws Exception {
        AIMasterData aiMasterData = new AIMasterData();
        BeanUtils.copyProperties(aiMasterDataAddRequest, aiMasterData);
        String botId = "7433646386040160268";
        String user = "user";
        String token = "pat_bU9pga0uXTGH8nHAwZ8QkIISSNiIvTPS580NZW8DjEdndxNx8BouusYhjpYMlJPN";
        // 校验
        Map<String,String> AIResult =  aiMasterDataService.getChartDataForCouZiChart(aiMasterData, request,botId,user,token);
        if(StringUtils.isBlank(aiMasterData.getUserTitle())){
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        aiMasterDataService.IsAiMessagesessionForCourse(AIResult,aiMasterDataAddRequest.getUserTitle());
        return ResultUtils.success(aiMasterData);
    }



    /**
     * AI问答信息列表
     *
     * @param aiMasterDataQueryRequest
     * @return
     */
    @PostMapping("/list/aiRole/ai/master/data")
    public BaseResponse<Page<AIMasterData>> listAiMasterDataByPage(@RequestBody AIMasterDataQueryRequest aiMasterDataQueryRequest, HttpServletRequest request) {
        long current = aiMasterDataQueryRequest.getCurrent();
        long size = aiMasterDataQueryRequest.getPageSize();


        Page<AIMasterData> aiRolesPage = aiMasterDataService.page(new Page<>(current, size),
                aiMasterDataService.getQueryWrapper(aiMasterDataQueryRequest,request));
        return ResultUtils.success(aiRolesPage);
    }
}
