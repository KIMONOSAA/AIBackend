package com.kimo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.aiRole.AIRoleQueryRequest;
import com.kimo.model.entity.AIResultRecording;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author Mr.kimo
 */
public interface AiResultRecordingService extends IService<AIResultRecording> {

    boolean validAiResultRecording(AIResultRecording aiResultRecording, HttpServletRequest request);

    QueryWrapper<AIResultRecording> getQueryWrapper(AIRoleQueryRequest postQueryRequest);
}
