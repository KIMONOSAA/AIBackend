package com.kimo.constant;

/**
 * @author Mr.kimo
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    public static final String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    public static final String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    public static final String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    public static final String BAN_ROLE = "ban";


    public static final Long USER_POINT_REGISTER = 50L;

    public static final Long USER_POINT_SIGN = 50L;

    public static final Long USER_VIP_POINT_SIGN = 100L;


    // endregion
}
