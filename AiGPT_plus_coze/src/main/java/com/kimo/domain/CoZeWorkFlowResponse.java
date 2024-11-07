
package com.kimo.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoZeWorkFlowResponse implements Serializable {
    
    /**
     * 返回码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String msg;
    
    /**
     * data 工作流执行结果
     */
    private String data;

    /**
     * debug_url 工作流试运行调试页面
     */
    private String debug_url;
}
