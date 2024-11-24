package com.kimo.ucenter.model.dto;

import com.kimo.ucenter.model.po.Roles;
import lombok.Data;

/**
 * @author Mr.kimo
 * @title UserListDto
 * @date 2024/11/22 19:16
 * @description TODO
 */
@Data
public class UserListDto extends UserDto{
    private Roles roles;
}
