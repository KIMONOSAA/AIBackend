
package com.kimo.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;


@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouZiCompletionRequest implements Serializable {
    
    /**
     * 智能体ID
     */
    @JsonProperty("bot_id")
    private String botId;
    
    /**
     * 用户id
     */
    @JsonProperty("user_id")
    private String userId;

    
    /**
     * 问 - 内容
     */
    @JsonProperty("additional_messages")
    private List<GouZiAdditionalMessages> AdditionalMessages;

    /**
     * 历史消息 上下文用
     */
    @JsonProperty("auto_save_history")
    private Boolean chatHistory;
    
    /**
     * 流动
     */
    @JsonProperty("stream")
    private Boolean stream;
    
    
}
