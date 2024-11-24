package com.kimo.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.api.dto.UserDto;
import com.kimo.model.dto.aiMessageSession.AIMessageSessionQueryRequest;
import com.kimo.model.dto.aiMessageSession.AIMessageSessionUpdateRequest;
import com.kimo.model.dto.aiRole.AIRoleUpdateRequest;

import com.kimo.model.entity.AIMessageSession;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author Mr.kimo
 */
public interface AIMessageSessionService extends IService<AIMessageSession> {


    UserDto getUserDtoForRedisOrLock(HttpServletRequest request, String type);

    boolean deleteAIMessageSession(long id, HttpServletRequest request);


    Wrapper<AIMessageSession> getQueryWrapper(AIMessageSessionQueryRequest aiMessageSessionQueryRequest, UserDto user);

    boolean validAIMessageSession(AIMessageSessionUpdateRequest aiRole, HttpServletRequest request);

    void addAIMessageSession(HttpServletRequest request, AIMessageSession aiRole);

    UserDto getAIMessageSessionByUser(String request);

    void fetchUpdatePoint(Long pointNumber, Long userId);
}
