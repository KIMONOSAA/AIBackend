package com.kimo.model.vo;

import lombok.Data;

/**
 * bi 的返回结果
 */
@Data
public class BiResponse {
    private String genChart;

    private String genResult;

    private Long chartId;

}