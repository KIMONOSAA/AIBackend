package com.kimo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;

import com.kimo.model.dto.UserPermissionDto;
import com.kimo.model.po.Permissions;
import com.kimo.service.PermissionsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Mr.kimo
 * @title PermissionsUtils
 * @date 2024/11/21 21:35
 * @description TODO
 */
@Component
public class PermissionsUtils {

    @Autowired
    @Lazy
    private PermissionsService permissionsService;

    // 定义静态 ObjectMapper 实例
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 检查是否是超级管理员
     */
    public void ensureSuperAdmin(UserDto userDto) {
        // 是否是超级管理员
        UserPermissionDto userPermissionDto = new UserPermissionDto();
        BeanUtils.copyProperties(userDto, userPermissionDto);
        List<Permissions> userPermissions = permissionsService.getUserPermissions(userPermissionDto);
        boolean exists = userPermissions.stream()
                .anyMatch(permission -> getCodeList(permission).contains("114514"));
        ThrowUtils.throwIf(!exists, ErrorCode.ADD_DATABASE_ERROR, "你不是超级管理员");
    }

    /**
     * 从 Permissions 对象中解析 code 列表
     */
    public List<String> getCodeList(Permissions permissions) {
        try {
            return OBJECT_MAPPER.readValue(permissions.getCode(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION, "Failed to parse JSON");
        }
    }
}
