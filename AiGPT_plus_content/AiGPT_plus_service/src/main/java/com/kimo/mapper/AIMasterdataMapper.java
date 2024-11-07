package com.kimo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.entity.AIMasterData;

import java.util.Optional;


/**
 * @author Mr.kimo
 */
public interface AIMasterdataMapper extends BaseMapper<AIMasterData> {

    Optional<AIMasterData> findAiMasterDataById(long id);
}




