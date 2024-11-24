package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.ContentConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.ThrowUtils;

import com.kimo.mapper.AiResultRecordingMapper;
import com.kimo.model.dto.aiRole.AIRoleQueryRequest;

import com.kimo.model.entity.AIResultRecording;
import com.kimo.service.AiResultRecordingService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mr.kimo
 */
@Service
public class AiResultRecordingServiceImpl extends ServiceImpl<AiResultRecordingMapper, AIResultRecording>
    implements AiResultRecordingService {

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    UserClient userClient;


    @Override
    public boolean validAiResultRecording(AIResultRecording aiResultRecording, HttpServletRequest request) {
        //获取用户对象
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        //特判
        ThrowUtils.throwIf(userDto == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(aiResultRecording == null,ErrorCode.PARAMS_ERROR);

        aiResultRecording.setUserId(userDto.getId());

        AIResultRecording oldAIResultRecording = this.getById(aiResultRecording.getId());
        if(oldAIResultRecording.getUserId() == aiResultRecording.getUserId()){
            return true;
        }

        return false;
    }

    /**
     * 获取查询包装类
     *
     * @param aiRoleQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<AIResultRecording> getQueryWrapper(AIRoleQueryRequest aiRoleQueryRequest) {
        QueryWrapper<AIResultRecording> queryWrapper = new QueryWrapper<>();
        if (aiRoleQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = aiRoleQueryRequest.getSearchText();
        String sortField = aiRoleQueryRequest.getSortField();
        String sortOrder = aiRoleQueryRequest.getSortOrder();
        Long id = aiRoleQueryRequest.getId();
        String aiRole = aiRoleQueryRequest.getAIRole();
        String description = aiRoleQueryRequest.getAIDescription();
        Long userId = aiRoleQueryRequest.getUserId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like(ContentConstant.AIROLE_NAME, searchText).or().like(ContentConstant.AIDESCRIPTION_NAME, searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(aiRole), ContentConstant.AIROLE_NAME, aiRole);
        queryWrapper.like(StringUtils.isNotBlank(description), ContentConstant.AIDESCRIPTION_NAME, description);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), SqlConstants.CHART_ID, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), SqlConstants.USERID_NAME, userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




