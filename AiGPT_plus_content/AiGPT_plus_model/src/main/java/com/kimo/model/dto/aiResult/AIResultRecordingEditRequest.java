package com.kimo.model.dto.aiResult;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 编辑请求
 *
 * @author kimo

 * 

 */
@Data
public class AIResultRecordingEditRequest implements Serializable {



    /**
     * ai角色
     */
    private String AIRole;

    /**
     * ai设定描述
     */
    private String AIDescription;

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

    /**
     * 是否公开
     */
    private boolean status;

    /**
     * 最大长度限制
     */
    private Integer AIMax_Tokens;



    private static final long serialVersionUID = 1L;
}