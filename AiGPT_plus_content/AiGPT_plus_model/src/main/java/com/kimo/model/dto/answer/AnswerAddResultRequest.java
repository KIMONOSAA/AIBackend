package com.kimo.model.dto.answer;

import lombok.Data;

import java.util.List;

@Data
public class AnswerAddResultRequest {
    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 用户传的答案数据
     */
    private List<QuestionListRequest> question;
}
