package com.kimo.ucenter.model.dto;

import lombok.Data;



@Data
public class UserPublishEventRequest {
    /**
     * 用户id
     */
    private long id;

    /**
     * 电子邮箱
     */
    private String email;
}
