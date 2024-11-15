package com.kimo.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.common.BaseResponse;
import com.kimo.common.DeleteRequest;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.constant.SecurityConstants;
import com.kimo.exception.BusinessException;
import com.kimo.mapper.AIMessageSessionMapper;

import com.kimo.model.dto.chart.AIMessageSessionAddRequest;
import com.kimo.model.dto.chart.AIMessageSessionQueryRequest;
import com.kimo.model.dto.chart.AIMessageSessionUpdateRequest;
import com.kimo.model.dto.chart.UserDto;
import com.kimo.model.dto.po.AIMessageSession;
import com.kimo.service.AIMessageSessionService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




/**
 * AI会话
 *
 * @author  kimo
 */
@RestController
@RequestMapping("/content")
@Slf4j
public class AIMessageSessionController {

    @Autowired
    private AIMessageSessionService aiMessageSessionService;
    

    @Autowired
    private AIMessageSessionMapper aiMessageSessionMapper;


    @Autowired
    private ServletUtils servletUtils;


    // region 增删改查

    /**
     * 创建新会话
     *
     * @param aiRoleAddRequest
     * @param
     * @return
     */
    @PostMapping("/add/ai/message")
    public BaseResponse<Long> addAIMessageSession(@RequestBody AIMessageSessionAddRequest aiRoleAddRequest, HttpServletRequest request) {

        if (aiRoleAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIMessageSession aiRole = new AIMessageSession();
        BeanUtils.copyProperties(aiRoleAddRequest, aiRole);
        // 校验
        aiMessageSessionService.addAIMessageSession(request,aiRole);
        long newAIMessageSessionId = 0;
        if(aiRole.getId() > 0){
            newAIMessageSessionId = aiRole.getId();
        }
        return ResultUtils.success(newAIMessageSessionId);
    }

    /**
     * 删除会话
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAIMessageSession(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        boolean isRole = aiMessageSessionService.deleteAIMessageSession(id,request);
        if(!isRole){
            ResultUtils.error(ErrorCode.SESSION_NOT_FOUND_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 修改会话标题
     *
     * @param aiRoleUpdateRequest
     * @param
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateAIMessageSession(@RequestBody AIMessageSessionUpdateRequest aiRoleUpdateRequest, HttpServletRequest request) {
        if (aiRoleUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        AIMessageSession aiRole = new AIMessageSession();
//        BeanUtils.copyProperties(aiRoleUpdateRequest, aiRole);
        // 参数校验
        boolean validAIMessageSession = aiMessageSessionService.validAIMessageSession(aiRoleUpdateRequest, request);
        if(!validAIMessageSession){
            return ResultUtils.error(ErrorCode.SESSION_NOT_FOUND_ERROR);
        }
        return ResultUtils.success(true);
    }



    /**
     * AI会话列表
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/list/aiRole")
    public BaseResponse<Page<AIMessageSession>> listAIMessageSessionByPage(@RequestBody AIMessageSessionQueryRequest postQueryRequest, HttpServletRequest request) {

        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = aiMessageSessionService.getAIMessageSessionByUser(username);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        Page<AIMessageSession> aiRolesPage = aiMessageSessionService.page(new Page<>(current, size),
                aiMessageSessionService.getQueryWrapper(postQueryRequest,userDto));
        return ResultUtils.success(aiRolesPage);
    }




}
