package com.kimo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.dto.TeachplanDto;
import com.kimo.model.dto.TeachplanListDto;
import com.kimo.model.po.Teachplan;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachplanListDto> getTeachPlanTreeNodeById(Long courseId);
}




