package com.kimo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.entity.AIResultRecording;

import java.util.Optional;

/**
 * @author Mr.kimo
 */
public interface AiResultRecordingMapper extends BaseMapper<AIResultRecording> {

    Optional<AIResultRecording> findAiResultRecordById(long id);
}




