package com.kimo.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Mr.kimo
 */
@Data
public class GenChartyByAIRequest implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;


    private static final long serialVersionUID = 1L;
}