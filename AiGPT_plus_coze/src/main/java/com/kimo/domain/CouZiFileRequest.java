package com.kimo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CouZiFileRequest {
    private String type;

    @JsonProperty("file_id")
    private String fileId;
}
