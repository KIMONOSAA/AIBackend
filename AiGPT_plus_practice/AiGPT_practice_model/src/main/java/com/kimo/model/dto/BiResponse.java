package com.kimo.model.dto;

import lombok.Data;

/**
 * bi 的返回结果
 */
@Data
public class BiResponse {
    private String genChart;

    private Double genResult;

    private Long chartId;

}
