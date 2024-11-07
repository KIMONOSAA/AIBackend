package com.kimo.ucenter.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *
 * @author kimo

 * 

 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;




    private static final long serialVersionUID = 1L;
}