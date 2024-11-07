package com.kimo.controller;

import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.exception.BusinessException;
import com.kimo.model.dto.answer.AnswerAddResultRequest;
import com.kimo.model.vo.BiResponse;
import com.kimo.service.AnswerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



// region 增删改查

/**
 *
 * 练习考试
 * @author  kimo
 */
@RestController
@RequestMapping("/content")
@Slf4j
public class AnswerController {


    @Autowired
    private AnswerService answerService;

    // region 增删改查

    /**
     * 练习提交（增加练习记录）
     *
     * @param answerAddResultRequest
     * @param
     * @return
     */
    @PostMapping("/add/answer")
    public BaseResponse<BiResponse> addQuestionResult(@RequestBody AnswerAddResultRequest answerAddResultRequest, HttpServletRequest request) throws Exception {

        if (answerAddResultRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
         BiResponse biResponse = answerService.generateAnswer(answerAddResultRequest, request);

        return ResultUtils.success(biResponse);
    }

    @RequestMapping("/r/r1")
    public String r1(){
        return "访问r1资源";
    }

}
