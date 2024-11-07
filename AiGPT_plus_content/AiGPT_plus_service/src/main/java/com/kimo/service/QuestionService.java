package com.kimo.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.question.QuestionQueryRequest;
import com.kimo.model.entity.Question;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Mr.kimo
 */
public interface QuestionService extends IService<Question> {

    void extractData(MultipartFile multipartFile);

    Wrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest, HttpServletRequest request);
}
