package com.kimo.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.annotation.PermissionMethod;
import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.mapper.AccuracyMapper;
import com.kimo.model.dto.*;
import com.kimo.model.po.Accuracy;
import com.kimo.model.po.Question;
import com.kimo.service.AccuracyService;
import com.kimo.service.PracticeAnswerService;
import com.kimo.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 *
 * 练习考试
 * @author  kimo
 */
@RestController
@Slf4j
public class QuestionController {
    

    @Autowired
    private QuestionService questionService;


    @Autowired
    private PracticeAnswerService answerService;

    @Autowired
    private AccuracyService accuracyService;



    
    
    // region 增删改查

    /**
     * 将excel提取数据到数据库中(题库添加)
     *
     * @param
     * @param
     * @return
     */
    @PostMapping("/add/question")
    @PermissionMethod(permission = "course_manager_course_all")
    public void addAiMasterData(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) throws Exception {
        questionService.extractData(multipartFile);
    }



    /**
     * 题目列表
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/question")
    public BaseResponse<QuestionListRecord> listQuestionDataByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        ThrowUtils.throwIf(current > 4,ErrorCode.PARAMS_ERROR);
        Map<String, Wrapper<Question>> queryWrapper = questionService.getQueryWrapper(questionQueryRequest, request);

        String firstKey = queryWrapper.keySet().stream().findFirst().orElse(null);
        System.out.println(queryWrapper.get(firstKey));
        Page<Question> aiRolesPage = questionService.page(new Page<>(current, size),
                queryWrapper.get(firstKey));
        QuestionListRecord questionListRecord = new QuestionListRecord();
        questionListRecord.setQuestions(aiRolesPage);
        questionListRecord.setRecord(firstKey);
        return ResultUtils.success(questionListRecord);
    }


    // region 增删改查

    /**
     * 练习下一页
     *
     * @param answerAddResultRequest
     * @param
     * @return
     */
    @PostMapping("/add/answer")
    public BaseResponse addQuestionResult(@RequestBody AnswerAddResultRequest answerAddResultRequest, HttpServletRequest request) throws Exception {

        if (answerAddResultRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = answerService.setResultAnswer(answerAddResultRequest,request);

        return ResultUtils.success(result);
    }


    /**
     * 提交结果（增加练习记录）
     *
     * @param answerAddResultRequest
     * @param
     * @return
     */
    @PostMapping("/add/answer/result")
    public BaseResponse<BiResponse> addQuestionResultOverall(@RequestBody AnswerAllResultRequest answerAddResultRequest, HttpServletRequest request) throws Exception {

        if (answerAddResultRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        BiResponse biResponse = answerService.generateAnswer(answerAddResultRequest, request);

        return ResultUtils.success(biResponse);
    }

    @GetMapping("/get/all/accuracy")
    public Accuracy addQuestionResultOverall(@RequestParam Long accuracyId)  {
        Accuracy accuracy = accuracyService.getById(accuracyId);
        return accuracy;
    }


}
