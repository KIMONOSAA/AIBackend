package com.kimo.model.dto;

import lombok.Data;

/**
 * @author Mr.kimo
 * @title UpdateQuestion
 * @date 2024/11/18 22:20
 * @description TODO
 */

@Data
public class UpdateQuestion extends AddQuestion{
    private Long questionId;
}
