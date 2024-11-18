package com.kimo.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.common.BaseResponse;
import com.kimo.common.DeleteRequest;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.exception.BusinessException;
import com.kimo.mapper.AiResultRecordingMapper;
import com.kimo.model.dto.aiResult.AIResultRecordingAddRequest;
import com.kimo.model.dto.aiRole.AIRoleQueryRequest;
import com.kimo.model.entity.AIResultRecording;
import com.kimo.service.AiResultRecordingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
public class AiResultRecordingController {

    @Autowired
    private AiResultRecordingService aiResultRecordingService;


    @Autowired
    private AiResultRecordingMapper aiResultRecordingMapper;

    // region 增删改查

    /**
     * 创建
     *
     * @param aiResultRecordingAddRequest
     * @param
     * @return
     */
    @PostMapping("/add/ai/result")
    public BaseResponse<Long> addAiResultRecording(@RequestBody AIResultRecordingAddRequest aiResultRecordingAddRequest, HttpServletRequest request) {
        if (aiResultRecordingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIResultRecording aiResultRecording = new AIResultRecording();
        BeanUtils.copyProperties(aiResultRecordingAddRequest, aiResultRecording);
        // 校验
        boolean isAiRole =  aiResultRecordingService.validAiResultRecording(aiResultRecording, request);
        if(isAiRole){
            aiResultRecordingMapper.insert(aiResultRecording);
        }
        long newAiResultRecordingId = aiResultRecording.getId();
        return ResultUtils.success(newAiResultRecordingId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete/ai/result")
    public BaseResponse<Boolean> deleteAiResultRecording(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        AIResultRecording oldAIResultRecording = aiResultRecordingMapper.findAiResultRecordById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        if (oldAIResultRecording == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        aiResultRecordingMapper.deleteById(id);
        return ResultUtils.success(true);
    }




    /**
     * 分页获取列表
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/list/aiRole/ai/result")
    public BaseResponse<Page<AIResultRecording>> listAiResultRecordingByPage(@RequestBody AIRoleQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        Page<AIResultRecording> aiRolesPage = aiResultRecordingService.page(new Page<>(current, size),
                aiResultRecordingService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(aiRolesPage);
    }

}
