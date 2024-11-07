package com.kimo.constant;

/**
 * @author Mr.kimo
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";


    Long USER_POINT_REGISTER = 50L;

    Long USER_POINT_SIGN = 50L;

    Long USER_VIP_POINT_SIGN = 100L;


    // endregion
}
