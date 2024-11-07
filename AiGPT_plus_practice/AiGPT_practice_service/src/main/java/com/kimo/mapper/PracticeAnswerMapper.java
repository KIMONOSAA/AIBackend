package com.kimo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.dto.PracticeAnswerDto;
import com.kimo.model.po.PracticeAnswer;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface PracticeAnswerMapper extends BaseMapper<PracticeAnswer> {

    List<PracticeAnswerDto> selectQuestion(Long practiceId);
}




