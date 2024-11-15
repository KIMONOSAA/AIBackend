package com.kimo.model.dto.chart;


import lombok.Data;

import java.io.Serializable;


/**
 * 创建请求
 *
 * @author kimo

 * 

 */
@Data
public class AIMasterDataAddRequest implements Serializable {

    /**
     * sessionId
     */
    private Long aiMessageSessionId;

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
     * 用户标题
     */
    private String userTitle;

    /**
     * 用户内容
     */
    private String userBody;




    private static final long serialVersionUID = 1L;
}