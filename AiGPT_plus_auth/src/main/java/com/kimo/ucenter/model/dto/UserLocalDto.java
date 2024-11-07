package com.kimo.ucenter.model.dto;

import com.kimo.ucenter.model.po.Role;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @TableName user
 */
@Data
public class UserLocalDto implements Serializable {
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
     * 用户昵称
     */
    private String userName;



}