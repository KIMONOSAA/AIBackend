package com.kimo.model.dto;



import com.kimo.common.PageRequest;
import lombok.Data;


@Data
public class QuestionQueryRequest extends PageRequest {

    private String subjects;

    private Long recordId;

    private Long teacherId;

    private Long courseId;

    private String qualifications;
}
