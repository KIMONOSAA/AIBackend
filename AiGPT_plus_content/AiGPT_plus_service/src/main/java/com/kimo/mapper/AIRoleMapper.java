package com.kimo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.entity.AIRole;

import java.util.Optional;

/**
 * @author Mr.kimo
 */
public interface AIRoleMapper extends BaseMapper<AIRole> {

    Optional<AIRole> findAIRoleById(long id);
}




