package com.kimo.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Mr.kimo
 * @title AddQuestion
 * @date 2024/11/18 22:16
 * @description TODO
 */
@Data
public class AddQuestion {
    /**
     * 科目
     */
    private String subjects;

    /**
     * 科目题目
     */
    private String subjectsTitle;

    /**
     * 题目的结果答案
     */
    private String subjectsResult;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 知识点
     */
    private String knowledge;

    /**
     * 选项a
     */
    private String optionA;

    /**
     * 选项B
     */
    private String optionB;

    /**
     * 选项C
     */
    private String optionC;

    /**
     * 选项D
     */
    private String optionD;
}
