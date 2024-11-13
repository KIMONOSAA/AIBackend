package com.kimo.dto;

import lombok.Data;

@Data
public class ResultData<T> {
    private boolean success;
    private int status;
    private String message;
    private T data;

    // Getter and Setter methods

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}
