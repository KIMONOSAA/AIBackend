package com.kimo.model.dto.question;

import com.kimo.common.PageRequest;
import lombok.Data;

@Data
public class QuestionQueryRequest extends PageRequest {

    private String subjects;

}
