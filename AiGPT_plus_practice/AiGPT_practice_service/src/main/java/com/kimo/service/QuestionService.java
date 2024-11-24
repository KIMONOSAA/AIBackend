package com.kimo.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.QuestionQueryRequest;
import com.kimo.model.po.Question;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
/**
 * @author Mr.kimo
 */
public interface QuestionService extends IService<Question> {

    void extractData(MultipartFile multipartFile,HttpServletRequest request);

    Map<String, Wrapper<Question>> getQueryWrapper(QuestionQueryRequest questionQueryRequest, HttpServletRequest request);

    Question selectById(Long questionId);

}
