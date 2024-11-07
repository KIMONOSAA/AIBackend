package com.kimo.model.dto;

import lombok.Data;

import java.io.Serializable;
/**
 * @author Mr.kimo
 */
@Data
public class UserPermissionDto implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 会员状态
     */
    private String member;

    private Long roleId;
}
