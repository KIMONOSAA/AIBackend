package com.kimo.model.dto;

import lombok.Data;

import java.io.Serializable;
/**
 * @author Mr.kimo
 */
@Data
public class UserPermissionDto implements Serializable {

    /**
     * id
     */
    private Long id;


    private Long roleId;
}
