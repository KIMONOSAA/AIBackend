package com.kimo.model.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionDataVO {
    /**
     * 科目
     */
    @ExcelProperty(value = "科目", index = 0)
    private String subjects;
    /**
     * 科目题目
     */
    @ExcelProperty(value = "题目", index = 1)
    private String subjectsTitle;

    /**
     * 科目的选择项
     */
    @ExcelProperty(value = "A", index = 2)
    private String optionA;

    @ExcelProperty(value = "B", index = 3)
    private String optionB;

    @ExcelProperty(value = "C", index = 4)
    private String optionC;

    @ExcelProperty(value = "D", index = 5)
    private String optionD;

    /**
     * 题目的结果答案
     */
    @ExcelProperty(value = "答案", index = 6)
    private String subjectsResult;
}
