package com.kimo.service;


import com.baomidou.mybatisplus.extension.service.IService;

import com.kimo.api.dto.UserDto;
import com.kimo.model.dto.AnswerAddResultRequest;
import com.kimo.model.dto.AnswerAllResultRequest;
import com.kimo.model.dto.BiResponse;
import com.kimo.model.dto.QuestionListRequest;
import com.kimo.model.po.PracticeAnswer;
import com.kimo.model.po.Question;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author Mr.kimo
 */
public interface PracticeAnswerService extends IService<PracticeAnswer> {

    Boolean setResultAnswer(AnswerAddResultRequest answerAddResultRequest, HttpServletRequest request);

    void createAnswerData(QuestionListRequest question, UserDto userDto, Long practiceId, Long courseId, Long teachplanId, Map<Long, Question> questionCache);

    BiResponse generateAnswer(AnswerAllResultRequest answerAddResultRequest, HttpServletRequest request);

}
