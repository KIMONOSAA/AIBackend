package com.kimo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图表信息表
 * @TableName aimasterdata
 */
@TableName(value ="aimasterdata")
@Data
public class AIMasterData implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * ai标题
     */
    private String aiTitle;

    /**
     * ai内容
     */
    private String aiBody;

    /**
     * ai结果
     */
    private String aiResult;

    /**
     * 消息会话Id
     */
    private Long aiMessageSessionId;

    /**
     * 用户标题
     */
    private String userTitle;

    /**
     * 用户内容
     */
    private String userBody;

    /**
     * 用户Id
     */
    private Long userId;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}