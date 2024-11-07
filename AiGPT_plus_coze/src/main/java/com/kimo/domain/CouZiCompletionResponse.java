
package com.kimo.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouZiCompletionResponse implements Serializable {
    
    /**
     * 返回码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String msg;
    
    /**
     * 会话id
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 返回内容
     */
    private List<Message> messages;
}
