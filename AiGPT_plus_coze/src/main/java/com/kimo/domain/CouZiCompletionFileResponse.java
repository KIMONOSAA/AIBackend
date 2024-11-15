package com.kimo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouZiCompletionFileResponse implements Serializable {
    @JsonProperty("code")
    private int code;
    @JsonProperty("data")
    private CouZiData data;
    @JsonProperty("msg")
    private String msg;


    @Data
    public class CouZiData {

        @JsonProperty("bytes")
        private long bytes;

        @JsonProperty("created_at")
        private long createdAt;
        @JsonProperty("file_name")
        private String fileName;
        @JsonProperty("id")
        private String id;
    }
}
