package com.kimo.aop;

import com.kimo.annotation.PermissionMethod;


import com.kimo.common.ErrorCode;
import com.kimo.constant.SecurityConstants;
import com.kimo.exception.BusinessException;
import com.kimo.feignclient.UserClient;
import com.kimo.model.dto.Permissions;
import com.kimo.model.dto.UserDto;
import com.kimo.model.dto.UserPermissionDto;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
/**
 * @author Mr.kimo
 */
@Aspect
@Component
@Slf4j
public class PermissionHandler {

    @Autowired
    private UserClient userClient;


    @Autowired
    private ServletUtils servletUtils;

    @Pointcut("@annotation(com.kimo.annotation.PermissionMethod)")
    public void permissionValue() {
    }

    @Around("permissionValue()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        PermissionMethod permissionMethod = methodSignature.getMethod().getAnnotation(PermissionMethod.class);
        String permission = permissionMethod.permission();
        String methodName = methodSignature.getName();
        Object[] objects = joinPoint.getArgs();
        //从当前线程获取HttpServletRequset
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        if (userDto == null) {
            throw new BusinessException(ErrorCode.USER_IS_NOT);
        }
        UserPermissionDto userPermissionDto = new UserPermissionDto();
        BeanUtils.copyProperties(userDto, userPermissionDto);
        List<Permissions> userPermissions = userClient.getUserPermissions(userPermissionDto);
        boolean hasPermission = userPermissions.stream()
                .anyMatch(permissions -> permissions.getCode().equals(permission));
        if (!hasPermission){
            throw new BusinessException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        return joinPoint.proceed();
    }

    
}
