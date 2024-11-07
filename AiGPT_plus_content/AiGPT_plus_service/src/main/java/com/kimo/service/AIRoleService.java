package com.kimo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.aiRole.AIRoleQueryRequest;
import com.kimo.model.dto.aiRole.AIRoleUpdateRequest;
import com.kimo.model.entity.AIRole;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mr.kimo
 */
public interface AIRoleService extends IService<AIRole> {

    boolean validAIRole(AIRole post, HttpServletRequest request);

    boolean deleteAIRole(long id, HttpServletRequest request);

    QueryWrapper<AIRole> getQueryWrapper(AIRoleQueryRequest postQueryRequest,HttpServletRequest request);

    boolean validUpdateAIRole(AIRoleUpdateRequest aiRoleUpdateRequest, HttpServletRequest request);
}

