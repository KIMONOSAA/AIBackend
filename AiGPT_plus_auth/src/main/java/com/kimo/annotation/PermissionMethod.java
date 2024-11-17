package com.kimo.annotation;

import java.lang.annotation.*;

/**
 * @Author kimo
 * @Description  自定义权限注解
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionMethod {
    /**
     * 权限值
     */
    String permission();
}