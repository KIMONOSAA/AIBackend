package com.kimo.model.dto.chart;

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
public class AIMasterDataQueryRequest extends PageRequest implements Serializable {


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 会话id
     */
    private Long aiMessageId;



    private static final long serialVersionUID = 1L;
}