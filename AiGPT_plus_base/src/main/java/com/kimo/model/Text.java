package com.kimo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
/**
 * @author Mr.kimo
 */
@Data
@Builder
public class Text {

    /**
     * 取值为[user,assistant]
     */
    private String role;
    /**
     * 所有content的累计tokens需控制8192以内
     */
    private String content;
    /**
     * 结果序号，取值为[0,10]; 当前为保留字段，开发者可忽略
     */
    private Integer index;

    /**
     * 图片理解才使用
     * 到底是图片还是问题描述
     */
    @JsonProperty("content_type")
    private String contentType;

    @Getter
    public enum Role {

        USER("user"),
        ASSISTANT("assistant"),
        ;

        Role(String name) {
            this.name = name;
        }

        private final String name;
    }
}
