package com.kimo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 准确率表
 * @TableName accuracy
 */
@TableName(value ="accuracy")
@Data
public class Accuracy implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 准确率
     */
    private String accuracy;


    /**
     * 创建表时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 学历
     */
    private String qualifications;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}