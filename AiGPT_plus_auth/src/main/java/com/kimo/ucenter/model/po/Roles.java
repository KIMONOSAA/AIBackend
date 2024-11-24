package com.kimo.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 角色
 * @TableName roles
 */
@TableName(value ="roles")
@Data
public class Roles implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色名
     */
    private String rolesName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}