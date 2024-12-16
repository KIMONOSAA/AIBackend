package com.kimo.ucenter.model.dto;

import lombok.Data;

import java.util.List;


@Data
public class PageResponse<T> {
    private long total;  // 总数
    private List<T> list;  // 分页数据

    // 构造函数、getter 和 setter 方法
}
