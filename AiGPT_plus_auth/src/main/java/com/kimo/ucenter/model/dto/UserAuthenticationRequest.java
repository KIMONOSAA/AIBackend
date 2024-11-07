package com.kimo.ucenter.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class UserAuthenticationRequest {
    private String email;
    private String password;
}
