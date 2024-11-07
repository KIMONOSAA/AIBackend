package com.kimo.model.dto;

import lombok.Data;

@Data
public class AnswerAllResultRequest {

    private Long teachplanId;

    private Long courseId;

    private Long practiceId;

    private String subjects;
}
