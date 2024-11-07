package com.kimo.ucenter.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 * @author kimo

 * 

 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String email;

    private String userPassword;
}
