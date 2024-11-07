package com.kimo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * @author Mr.kimo
 */
@TableName(value ="course_market")
@Data
public class CourseMarket implements Serializable {
    /**
     * 主键，课程id
     */
    @TableId
    private Long id;

    /**
     * 收费规则，对应数据字典
     */
    private String charge;


    /**
     * 会员价
     */
    private Long memberPrice;

    /**
     * 咨询qq
     */
    private String qq;

    /**
     * 微信
     */
    private String wechat;

    /**
     * 电话
     */
    private String phone;

    /**
     * 有效期天数
     */
    private Integer validDays;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}