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

import static com.kimo.constans.CouZiConstant.AI_QUESTION_AND_ANSWER_BOT_ID;
import static com.kimo.constans.CouZiConstant.AI_QUESTION_AND_ANSWER_BOT_TOKEN;


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





    @PostMapping("/add/ai/master/data")
    /**
     * @Author: Mr.kimo
     * @Date: 12:05
     * @return: com.kimo.common.BaseResponse<com.kimo.model.dto.po.AIMasterData>
     * @Param: [com.kimo.model.dto.chart.AIMasterDataAddRequest, jakarta.servlet.http.HttpServletRequest]
     * @Description: AI问答
     */
    public BaseResponse<AIMasterData> addAiMasterData(@RequestBody AIMasterDataAddRequest aiMasterDataAddRequest, HttpServletRequest request) throws Exception {
        AIMasterData aiMasterData = new AIMasterData();
        BeanUtils.copyProperties(aiMasterDataAddRequest, aiMasterData);
        String botId = AI_QUESTION_AND_ANSWER_BOT_ID;
        String user = "user";
        String token = AI_QUESTION_AND_ANSWER_BOT_TOKEN;
        // 校验
        Map<String,String> AIResult =  aiMasterDataService.getChartDataForCouZiChart(aiMasterData, request,botId,user,token);
        if(StringUtils.isBlank(aiMasterData.getUserBody())){
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        aiMasterDataService.IsAiMessagesessionForCourse(AIResult,aiMasterData.getUserBody());
        return ResultUtils.success(aiMasterData);
    }




    @PostMapping("/list/aiRole/ai/master/data")
    /**
     * @Author: Mr.kimo
     * @Date: 12:05
     * @return: com.kimo.common.BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.kimo.model.dto.po.AIMasterData>>
     * @Param: [com.kimo.model.dto.chart.AIMasterDataQueryRequest, jakarta.servlet.http.HttpServletRequest]
     * @Description: 列出当前用户的所以与AI交互的信息分页获取
     */
    public BaseResponse<Page<AIMasterData>> listAiMasterDataByPage(@RequestBody AIMasterDataQueryRequest aiMasterDataQueryRequest, HttpServletRequest request) {
        long current = aiMasterDataQueryRequest.getCurrent();
        long size = aiMasterDataQueryRequest.getPageSize();


        Page<AIMasterData> aiRolesPage = aiMasterDataService.page(new Page<>(current, size),
                aiMasterDataService.getQueryWrapper(aiMasterDataQueryRequest,request));
        return ResultUtils.success(aiRolesPage);
    }
}
