package com.kimo.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CouZiMessageAndMasterRequest extends AImasterDataRequest{

    private String role;


    private String content;


    private String content_type;
}
