package com.kimo.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员表
 * @TableName user_member
 */
@TableName(value ="user_member")
@Data
public class UserMember implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员类型
     */
    private String memberType;

    /**
     * 会员介绍
     */
    private String memberDescribes;

    /**
     * 会员价钱
     */
    private Long memberPrice;

    /**
     * 会员名称
     */
    private String memberName;

    /**
     * 有效日期
     */
    private String expirationDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}