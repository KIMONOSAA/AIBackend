package com.kimo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.po.Accuracy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface AccuracyMapper extends BaseMapper<Accuracy> {

    int getAccuracyCount();

    List<Accuracy> getAccuracyListByPage(@Param("page") Integer page, @Param("size") Integer size);
}




