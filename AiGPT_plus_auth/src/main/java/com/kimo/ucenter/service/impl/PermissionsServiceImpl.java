//package com.kimo.ucenter.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.Wrapper;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//
//import com.kimo.common.ErrorCode;
//import com.kimo.constant.CommonConstant;
//import com.kimo.exception.BusinessException;
//import com.kimo.ucenter.mapper.PermissionsMapper;
//import com.kimo.ucenter.mapper.RolesMapper;
//import com.kimo.ucenter.model.dto.AddPermission;
//import com.kimo.ucenter.model.dto.UserDto;
//import com.kimo.ucenter.model.dto.UserPermissionDto;
//import com.kimo.ucenter.model.dto.UserPermissionQueryRequest;
//import com.kimo.ucenter.model.po.Permissions;
//import com.kimo.ucenter.model.po.Roles;
//import com.kimo.ucenter.model.po.User;
//import com.kimo.ucenter.service.PermissionsService;
//import com.kimo.ucenter.service.RolesService;
//import com.kimo.ucenter.service.UserService;
//import com.kimo.utils.SqlUtils;
//import jakarta.servlet.http.HttpServletRequest;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Lookup;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
///**
//* @author chen
//* @description 针对表【permissions(权限)】的数据库操作Service实现
//* @createDate 2024-10-31 11:18:07
//*/
//@Service
//public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions>
//    implements PermissionsService {
//
//    @Autowired
//    private RolesMapper rolesMapper;
//
//    @Autowired
//    private PermissionsMapper permissionsMapper;
//
//    @Autowired
//    @Lazy
//    private UserService userService;
//
//    @Override
//    public List<Permissions> getUserPermissions(UserPermissionDto user) {
//        Roles roles = rolesMapper.selectById(user.getRoleId());
//        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("role_id", roles.getId());
//
//        return permissionsMapper.selectList(queryWrapper);
//    }
//
//    @Override
//    public Wrapper<Permissions> getQueryWrapper(UserPermissionQueryRequest userPermissionQueryRequest) {
//        // 检查请求参数是否为空
//        if (userPermissionQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
//        }
//
//        String sortField = userPermissionQueryRequest.getSortField();
//        String sortOrder = userPermissionQueryRequest.getSortOrder();
//
//        // 初始化QueryWrapper
//        QueryWrapper<Permissions> queryWrapper = new QueryWrapper<>();
//
//        // 排序字段和顺序的处理
//        if (StringUtils.isNotBlank(sortField)) {
//            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
//            queryWrapper.orderBy(SqlUtils.validSortField(sortField), isAsc, sortField);
//        }
//
//        return queryWrapper;
//    }
//
//    @Override
//    public Boolean bindPermission(HttpServletRequest request) {
//        UserDto userFromCacheOrDb = userService.getUserFromCacheOrDb(request);
//
//        return null;
//    }
//
//    @Override
//    public Boolean createPermission(AddPermission permission) {
//        return null;
//    }
//
//    @Override
//    public Boolean deletedPermission(Long permissionId, HttpServletRequest request) {
//        return null;
//    }
//
//    @Override
//    public Permissions getPermission(Long permissionId, HttpServletRequest request) {
//        return null;
//    }
//}
//
//
//
//
