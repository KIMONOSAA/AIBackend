package com.kimo.ucenter.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author kimo

 * 

 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

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
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

//    /*
//     *学历
//     */
//    private String degree;


    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}