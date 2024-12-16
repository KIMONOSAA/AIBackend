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


import com.kimo.mapper.PermissionsMapper;
import com.kimo.mapper.RolesMapper;
import com.kimo.model.dto.AddPermission;

import com.kimo.model.dto.UserPermissionDto;
import com.kimo.model.dto.UserPermissionQueryRequest;
import com.kimo.model.po.Permissions;
import com.kimo.model.po.Roles;
import com.kimo.service.PermissionsService;
import com.kimo.utils.JwtService;
import com.kimo.utils.PermissionsUtils;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author chen
* @description 针对表【permissions(权限)】的数据库操作Service实现
* @createDate 2024-10-31 11:18:07
*/
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions>
    implements PermissionsService {

    @Autowired
    private RolesMapper rolesMapper;

    @Autowired
    private PermissionsMapper permissionsMapper;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PermissionsUtils permissionsUtils;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Override
    public List<Permissions> getUserPermissions(UserPermissionDto user) {
        Roles roles = rolesMapper.selectById(user.getRoleId());
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roles.getRoleId());

        return permissionsMapper.selectList(queryWrapper);
    }

    @Override
    public Wrapper<Permissions> getQueryWrapper(UserPermissionQueryRequest userPermissionQueryRequest,HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"114514");
        // 检查请求参数是否为空
        if (userPermissionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String sortField = userPermissionQueryRequest.getSortField();
        String sortOrder = userPermissionQueryRequest.getSortOrder();

        // 初始化QueryWrapper
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();

        // 排序字段和顺序的处理
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            queryWrapper.orderBy(SqlUtils.validSortField(sortField), isAsc, sortField);
        }

        return queryWrapper;
    }

    @Override
    public Boolean bindPermission(HttpServletRequest request) {
        UserDto userFromCacheOrDb = this.getUserFromCacheOrDb(request);

        return null;
    }

    @Override
    public Boolean createPermission(AddPermission permission,HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"114514");

        String codeJson = null;
        ArrayList<String> codeList = new ArrayList<>();
        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", permission.getRoleId());
        Permissions permissions1 = permissionsMapper.selectOne(queryWrapper);
        if(permissions1 == null){
            Permissions permissions = new Permissions();
            BeanUtils.copyProperties(permission, permissions);
            codeList.add(permissions.getCode());

            try {
                codeJson = OBJECT_MAPPER.writeValueAsString(codeList);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
            }
            permissions.setCode(codeJson);
            int insert = permissionsMapper.insert(permissions);

            return insert > 0;
        }
        try {
            codeList = OBJECT_MAPPER.readValue(permissions1.getCode(), new TypeReference<ArrayList<String>>() {
            });

            codeList.add(permission.getCode());
            codeJson = OBJECT_MAPPER.writeValueAsString(codeList);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
        }

        permissions1.setCode(codeJson);
        int i = permissionsMapper.updateById(permissions1);

        return i > 0;

    }

    private UserDto getaBoolean(HttpServletRequest request) {
        //获取用户信息
        UserDto userFromCacheOrDb = this.getUserFromCacheOrDb(request);

        permissionsUtils.ensureSuperAdmin(userFromCacheOrDb);

        return userFromCacheOrDb;
    }

    @Override
    public Boolean deletedPermission(Long permissionId, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"114514");
        int i = permissionsMapper.deleteById(permissionId);
        return i == 1;
    }

    @Override
    public Permissions getPermission(Long permissionId, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"114514");
        return permissionsMapper.selectById(permissionId);
    }

    @Override
    public UserDto getUserFromCacheOrDb(HttpServletRequest request) {
        // 从 Redis 缓存中获取用户信息
        Object userFromCache = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        // 如果缓存中没有找到，查询数据库
        if (userFromCache == null) {
            String userEmail = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
            if (userEmail == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法提取用户信息");
            }

            // 查询数据库中的用户
//            User userFromDb = userMapper.selectById(userEmail);
            UserDto userDto = userClient.GobalGetLoginUser(userEmail);
            if (userDto == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "没有这个用户");
            }

            // 将查询到的数据库用户信息转到 DTO
            return userDto;
        }

        // 如果缓存中找到了用户信息，直接转换为 UserDto
        return (UserDto) userFromCache;
    }

    @Override
    public Permissions getPermissionByRoleId(String roleId) {
        QueryWrapper<Roles> queryWrapperRole = new QueryWrapper<>();
        queryWrapperRole.eq("user_id", roleId);
        Roles roles = rolesMapper.selectOne(queryWrapperRole);
        Long roleId1 = roles.getRoleId();

        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId1);
        Permissions permissions = permissionsMapper.selectOne(queryWrapper);
        return permissions;
    }

    @Override
    public Boolean deletedPermissionForOne(Long permissionId, String roleCode, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String codet = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(codet,"114514");
        Permissions permissions = permissionsMapper.selectById(permissionId);
        if(permissions == null){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"未创建权限");
        }
        List<String> codeList = permissionsUtils.getCodeList(permissions);
        String code = null;
        codeList.remove(roleCode);
        try {
            code = OBJECT_MAPPER.writeValueAsString(codeList);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"进行权限json报错");
        }
        permissions.setCode(code);
        int i = permissionsMapper.updateById(permissions);
        return i == 1;
    }

    @Override
    public Permissions getPermissionByRoleIdForMember(String roleId) {

            QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("role_id", roleId);
            Permissions permissions = permissionsMapper.selectOne(queryWrapper);
            return permissions;

    }
}




