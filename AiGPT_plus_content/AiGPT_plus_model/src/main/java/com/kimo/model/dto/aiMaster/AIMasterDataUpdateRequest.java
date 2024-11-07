package com.kimo.model.dto.aiMaster;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author kimo

 * 

 */
@Data
public class AIMasterDataUpdateRequest implements Serializable {


    /**
     * 用户标题
     */
    private String userTitle;

    /**
     * 用户内容
     */
    private String userBody;

    /**
     * 最大长度限制
     */
    private Integer AIMax_Tokens;


    private static final long serialVersionUID = 1L;
}