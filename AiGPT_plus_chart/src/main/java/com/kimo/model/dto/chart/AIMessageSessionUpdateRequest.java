package com.kimo.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author kimo

 * 

 */
@Data
public class AIMessageSessionUpdateRequest implements Serializable {



    private Long id;

    /**
     * 主会话标题
     */
    private String title;

    /**
     * 用户Id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}