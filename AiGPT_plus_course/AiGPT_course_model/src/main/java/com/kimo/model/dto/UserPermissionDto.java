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

    private Long roleId;


    private String qualification;

    private String grade;

    /**
     * USER/Admin
     */
    private String userRole;

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
     * 是否第一次注册获得积分
     */
    private Integer isPoint;


    /**
     * 是否验证过
     */
    private Integer isEnable;

}