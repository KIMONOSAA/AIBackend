package com.kimo.model.dto.answer;


import lombok.Data;

@Data
public class WrongInformation {

    /**
     * 科目
     */
    private String subjects;

    /**
     * 答错的题目
     */
    private String subjectsTitle;

    /**
     * 科目的选择项
     */
    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    /**
     * 题目的错误结果答案
     */
    private String subjectsWrong;

    /**
     * 题目的正确结果答案
     */
    private String subjectsResult;

}
