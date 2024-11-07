package com.kimo.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnswerAddResultRequest {


    private Long TeachplanId;

    private Long courseId;

    private Long practiceId;

    private String subjects;

    /**
     * 用户传的答案数据
     */
    private List<QuestionListRequest> question;
}
