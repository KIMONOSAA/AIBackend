package com.kimo.ucenter.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthentianResponse {

    private String accessToken;

    private String refershToken;
}
