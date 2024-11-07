package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.exception.ThrowUtils;
import com.kimo.model.dto.question.QuestionQueryRequest;
import com.kimo.model.entity.Question;


import com.kimo.service.QuestionService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * 练习考试
 * @author  kimo
 */
@RestController
@RequestMapping("/content")
@Slf4j
public class QuestionController {
    

    @Autowired
    private QuestionService questionService;


    
    
    // region 增删改查

    /**
     * 将excel提取数据到数据库中(题库添加)
     *
     * @param
     * @param
     * @return
     */
    @PostMapping("/add/question")
    public void addAiMasterData(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) throws Exception {
//        User user = userService.getLoginUser(request);
//        ThrowUtils.throwIf(user == null,ErrorCode.NOT_LOGIN_ERROR);
        questionService.extractData(multipartFile);
    }



    /**
     * 题目列表
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/question")
    public BaseResponse<Page<Question>> listQuestionDataByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        ThrowUtils.throwIf(current > 4,ErrorCode.PARAMS_ERROR);

        Page<Question> aiRolesPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest,request));
        return ResultUtils.success(aiRolesPage);
    }


}
