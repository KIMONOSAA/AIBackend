package com.kimo.utils;

import com.kimo.common.ErrorCode;
import com.kimo.exception.ThrowUtils;

/**
 * @author Mr.kimo
 * @title CommonUtils
 * @date 2024/11/17 9:31
 * @description TODO
 */
public class CommonUtils {

    /**
     * @Author: Mr.kimo
     * @Date: 9:32
     * @return: T
     * @Param: object - 要检查的对象
     * @Param: errorCode - 错误码
     * @Description: 通用的空值检查方法，如果对象为空，则抛出指定错误码的异常
     */
    public static <T> T checkIfNull(T object, ErrorCode errorCode) {
        ThrowUtils.throwIf(object == null, errorCode);
        return object;
    }
}
