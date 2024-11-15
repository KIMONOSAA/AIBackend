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
public class AIMessageSessionAddRequest implements Serializable {


    /**
     * 主会话标题
     */
    private String title;



    private static final long serialVersionUID = 1L;
}