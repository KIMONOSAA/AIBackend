package com.kimo.ucenter.model.dto;

import com.kimo.ucenter.model.po.Role;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author kimo

 * 

 */
@Data
public class UserUpdateRequest implements Serializable  {

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

    /**
     * 用户角色：user/admin/ban
     */
    private Role userRole;

    private static final long serialVersionUID = 1L;
}