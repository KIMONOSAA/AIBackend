
package com.kimo.common;


import lombok.Getter;


public class Constants {
    /**
     * 空
     */
    public final static String NULL = "NULL";

    /**
     * 鉴权
     */
    public final static String AUTHORIZATION = "Authorization";
    /**
     * token前缀
     */
    public final static String BEARER = "Bearer ";

    /**
     * 角色
     * 官网支持的请求角色类型；user、assistant
     */
    @Getter
    public enum Role {

        /**
         * 使用者
         */
        USER("user"),
        /**
         * 助理
         */
        ASSISTANT("assistant"),
        ;

        /**
         * 密码
         */
        private final String code;

        /**
         * 角色
         *
         * @param code 密码
         */
        Role(String code) {
            this.code = code;
        }

    }

    /**
     * 问答类型
     */
    @Getter
    public enum Type {
        
        /**
         * 文本
         */
        TEXT("text"),
        /**
         * img
         */
        IMG("file_url")
        ;

        /**
         * 密码
         */
        private final String code;

        /**
         * 角色
         *
         * @param code 密码
         */
        Type(String code) {
            this.code = code;
        }

    }

}
