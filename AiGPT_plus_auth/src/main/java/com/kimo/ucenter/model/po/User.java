package com.kimo.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;


/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private String qualification;

    private String grade;

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


    private Long roleId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private byte[] userAvatar;

    /**
     * 会员状态
     */
    private String member;

    /**
     * 用户角色：user/admin/ban
     */
    private Role userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否第一次注册获得积分
     */
    private Integer isPoint;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 是否验证过
     */
    private Integer isEnable;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}