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

import com.kimo.model.dto.aiMaster.AIMasterDataAddRequest;
import com.kimo.model.dto.aiMaster.AIMasterDataQueryRequest;
import com.kimo.model.entity.AIMasterData;
import com.kimo.model.entity.AIMessageSession;
import com.kimo.service.AIMasterDataService;
import com.kimo.service.AIMessageSessionService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/content")
@Slf4j
/**
 * @Author: Mr.kimo
 * @Date: 18:25
 * @return:
 * @Param:
 * @Description: 已更新版本，当前模块用到的模块已经移到到chart这个服务中
 */
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
        Long aiId = aiMasterDataAddRequest.getAiMessageSessionId();
        QueryWrapper<AIMessageSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.CHART_ID,aiId);
        //会话id是否存在
        AIMessageSession aiMessageSession = aiMessageSessionService.getOne(queryWrapper);
        ThrowUtils.throwIf(aiMessageSession == null,ErrorCode.NOT_FOUND_ERROR);
        AIMasterData aiMasterData = new AIMasterData();
        BeanUtils.copyProperties(aiMasterDataAddRequest, aiMasterData);
        String botId = "7433646386040160268";
        String user = "user";
        String token = "pat_bU9pga0uXTGH8nHAwZ8QkIISSNiIvTPS580NZW8DjEdndxNx8BouusYhjpYMlJPN";
        // 校验

        String AIResult =  aiMasterDataService.getChartDataForCouZiChart(aiMasterData, request,botId,user,token);
        if(StringUtils.isNotBlank(AIResult)){
            aiMasterData.setAiResult(AIResult);
            aiMasterdataMapper.insert(aiMasterData);
        }
        if(StringUtils.isBlank(aiMasterData.getUserTitle())){
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(aiMasterData);
    }

    /**
     * todo(修改)
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete/ai/master/data")
    public BaseResponse<Boolean> deleteAiMasterData(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        AIMasterData oldAIMasterData = aiMasterdataMapper.selectById(id);
        if(oldAIMasterData == null){
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        aiMasterdataMapper.deleteById(id);
        return ResultUtils.success(true);
    }




    /**
     * AI问答信息列表
     *
     * @param aiMasterDataQueryRequest
     * @return
     */
    @PostMapping("/list/aiRole/ai/master/data")
    public BaseResponse<Page<AIMasterData>> listAiMasterDataByPage(@RequestBody AIMasterDataQueryRequest aiMasterDataQueryRequest,HttpServletRequest request) {
        long current = aiMasterDataQueryRequest.getCurrent();
        long size = aiMasterDataQueryRequest.getPageSize();


        Page<AIMasterData> aiRolesPage = aiMasterDataService.page(new Page<>(current, size),
                aiMasterDataService.getQueryWrapper(aiMasterDataQueryRequest,request));
        return ResultUtils.success(aiRolesPage);
    }
}
