package com.kimo.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.kimo.api.dto.UserDto;
import com.kimo.common.BaseResponse;
import com.kimo.common.DeleteRequest;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.constant.SecurityConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.mapper.AccuracyMapper;
import com.kimo.model.dto.*;
import com.kimo.model.po.Accuracy;
import com.kimo.model.po.Question;
import com.kimo.service.AccuracyService;
import com.kimo.service.PracticeAnswerService;
import com.kimo.service.QuestionService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    @Autowired
    private ServletUtils servletUtils;



    
    
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
        questionService.extractData(multipartFile,request);
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



    /**
     * 题目列表
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/manager/question")
    public BaseResponse<QuestionListRecord> listQuestionDataByPageForMember(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {

        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"1000002");
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
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

    //添加题目
    @PostMapping("/add/practice")
    /**
     * @Author kimo
     * @Description  创建用户只允许管理员
     * @Date
     * @Param
     * @param userAddRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<java.lang.Long>
     **/
    public BaseResponse<Long> addUser(@RequestBody AddQuestion addQuestion, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"1000001");
        if (addQuestion == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(addQuestion, question);
        question.setCreateTime(LocalDateTime.now());
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(question.getId());
    }


    //删除题目
    @PostMapping("/delete")
    /**
     * @Author kimo
     * @Description  删除用户只允许管理员
     * @Date
     * @Param
     * @param deleteRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<java.lang.Boolean>
     **/
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"1000004");
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = questionService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    //更新题目
    @PostMapping("/update")
    /**
     * @Author kimo
     * @Description  修改用户只允许管理员
     * @Date
     * @Param
     * @param userUpdateRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<java.lang.Boolean>
     **/
    public BaseResponse<Boolean> updateUser(@RequestBody UpdateQuestion updateQuestion,
                                            HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"1000003");
        if (updateQuestion == null || updateQuestion.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.selectById(updateQuestion.getQuestionId());
        ThrowUtils.throwIf(question == null,ErrorCode.USER_IS_NOT);
        BeanUtils.copyProperties(updateQuestion, question);
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    //查看题目
    @PostMapping("/get/{questionId}")
    /**
     * @Author kimo
     * @Description  管理员、获取用户
     * @Date
     * @Param
     * @param id
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<com.kimo.ucenter.model.po.User>
     **/
    public BaseResponse<Question> getUserById(@PathVariable("questionId") Long questionId, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"1000002");
        if (questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question user = questionService.getById(questionId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }


}
