package com.kimo.annotation;

import java.lang.annotation.*;
/**
 * @author Mr.kimo
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionMethod {
    /**
     * 权限值
     */
    String permission();
}