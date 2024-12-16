package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;

import com.kimo.constant.SecurityConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;

import com.kimo.mapper.RolesMapper;
import com.kimo.model.dto.AddRole;



import com.kimo.model.dto.UserRoleQueryRequest;
import com.kimo.model.po.Permissions;
import com.kimo.model.po.Roles;
import com.kimo.service.PermissionsService;
import com.kimo.service.RolesService;
import com.kimo.utils.PermissionsUtils;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



/**
* @author chen
* @description 针对表【roles(角色)】的数据库操作Service实现
* @createDate 2024-10-31 11:18:07
*/
@Service
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles>
    implements RolesService {


    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private RolesMapper rolesMapper;

    @Autowired
    private PermissionsUtils permissionsUtils;


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Boolean deletedRole(Long roleId, HttpServletRequest request) {
        //获取用户信息
        UserDto userFromCacheOrDb = permissionsService.getUserFromCacheOrDb(request);

        permissionsUtils.ensureSuperAdmin(userFromCacheOrDb);

        //删除角色
        Roles roles = rolesMapper.selectById(roleId);
        return rolesMapper.deleteById(roles) == 1;

    }



    @Override
    public Boolean createdRole(AddRole role, HttpServletRequest request) {
        //获取用户信息
        UserDto userFromCacheOrDb = permissionsService.getUserFromCacheOrDb(request);
        String code = servletUtils.getRoleForPermission(userFromCacheOrDb);

        servletUtils.ensuperAdminOrAdmin(code,"114514");
        ThrowUtils.throwIf(userFromCacheOrDb == null,ErrorCode.ADD_DATABASE_ERROR);
        ArrayList<String> strings = new ArrayList<String>();
        String rolseJson = null;
//        permissionsUtils.ensureSuperAdmin(userFromCacheOrDb);
        QueryWrapper<Roles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", role.getUserId());
        Roles roles1 = rolesMapper.selectOne(queryWrapper);
        if (roles1 == null) {
            Roles roles = new Roles();
            BeanUtils.copyProperties(role, roles);
            strings.add(role.getRolesName());
            try {
                rolseJson = OBJECT_MAPPER.writeValueAsString(strings);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION,"数据库数据格式错误");
            }
            roles.setRolesName(rolseJson);
            int insert = rolesMapper.insert(roles);
            return insert == 1;
        }


        String rolesName = roles1.getRolesName();
        try {
            strings = OBJECT_MAPPER.readValue(rolesName, new TypeReference<ArrayList<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION,"数据库数据格式错误");
        }
        strings.add(role.getRolesName());
        try {
            rolseJson = OBJECT_MAPPER.writeValueAsString(strings);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION,"数据库数据格式错误");
        }
        roles1.setRolesName(rolseJson);

        int i = rolesMapper.updateById(roles1);
        return i == 1;
    }

    @Override
    public Wrapper<Roles> getQueryWrapper(UserRoleQueryRequest userPermissionQueryRequest,HttpServletRequest request) {
        UserDto userFromCacheOrDb = permissionsService.getUserFromCacheOrDb(request);
        String code = servletUtils.getRoleForPermission(userFromCacheOrDb);

        servletUtils.ensuperAdminOrAdmin(code,"114514");
        QueryWrapper<Roles> queryWrapper = new QueryWrapper<>();
        //获取用户信息

        ThrowUtils.throwIf(userFromCacheOrDb == null,ErrorCode.NOT_FOUND_ERROR);


        // 获取排序字段和排序顺序
        String sortField = userPermissionQueryRequest.getSortField();
        String sortOrder = userPermissionQueryRequest.getSortOrder();

        handleSort(queryWrapper, sortField, sortOrder);
        return queryWrapper;
    }

    @Override
    public Roles getRolesById(String roleId,HttpServletRequest request) {
        UserDto userFromCacheOrDb = permissionsService.getUserFromCacheOrDb(request);
        String code = servletUtils.getRoleForPermission(userFromCacheOrDb);

        servletUtils.ensuperAdminOrAdmin(code,"114514");
        return rolesMapper.selectById(roleId);
    }

    @Override
    public Boolean getUserIsAdmin(HttpServletRequest request) {
        UserDto userFromCacheOrDb = permissionsService.getUserFromCacheOrDb(request);
        String code = servletUtils.getRoleForPermission(userFromCacheOrDb);
        ArrayList<String> strings = new ArrayList<>();
        servletUtils.ensuperAdminOrAdmin(code,"114514");
        Long id = userFromCacheOrDb.getId();
        QueryWrapper<Roles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        Roles roles = rolesMapper.selectOne(queryWrapper);
        if (roles == null) {
            return false;
        }
        String rolesName = roles.getRolesName();
        try {
            strings = OBJECT_MAPPER.readValue(rolesName,new TypeReference<ArrayList<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return strings.contains("ADMIN");

    }

    /**
     * 处理排序逻辑
     */
    private void handleSort(QueryWrapper<Roles> queryWrapper, String sortField, String sortOrder) {
        if (SqlUtils.validSortField(sortField)) {
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                queryWrapper.orderByAsc(sortField); // 按升序排序
            } else if (CommonConstant.SORT_ORDER_DESC.equals(sortOrder)) {
                queryWrapper.orderByDesc(sortField); // 按降序排序
            }
        } else {
            // 若排序字段无效，可以根据需求抛出异常或使用默认排序
            throw new BusinessException(ErrorCode.INVALID_SORT_FIELD, "无效的字段：" + sortField);
        }
    }
}




