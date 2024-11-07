package com.kimo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.answer.AnswerAddResultRequest;
import com.kimo.model.entity.Answer;
import com.kimo.model.vo.BiResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mr.kimo
 */
public interface AnswerService extends IService<Answer> {

    BiResponse generateAnswer(AnswerAddResultRequest answerAddResultRequest, HttpServletRequest request) throws Exception;

}
