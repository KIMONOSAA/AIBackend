package com.kimo.model.dto.aiMessageSession;

import com.kimo.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 * @author kimo

 * 

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AIMessageSessionQueryRequest extends PageRequest implements Serializable {


    /**
     * 主会话标题
     */
    private String title;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;




    private static final long serialVersionUID = 1L;
}