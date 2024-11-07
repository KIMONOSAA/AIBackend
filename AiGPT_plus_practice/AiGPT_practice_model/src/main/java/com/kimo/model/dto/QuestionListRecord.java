package com.kimo.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.model.po.Question;
import lombok.Data;

@Data
public class QuestionListRecord {
    private Page<Question> questions;
    private String record;
}
