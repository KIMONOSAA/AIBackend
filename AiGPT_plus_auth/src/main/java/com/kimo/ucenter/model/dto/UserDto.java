package com.kimo.ucenter.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.kimo.ucenter.model.po.Role;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户
 * @TableName user
 */
@Data
public class UserDto implements Serializable{
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