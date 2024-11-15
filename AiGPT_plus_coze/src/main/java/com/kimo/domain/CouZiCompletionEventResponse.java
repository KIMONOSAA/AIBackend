

package com.kimo.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;


//{
//        "id": "7433032055723933734",
//        "conversation_id": "7433032055723868198",
//        "bot_id": "7432966743104520192",
//        "role": "assistant",
//        "type": "answer",
//        "content": "{\"",
//        "content_type": "text",
//        "chat_id": "7433032055723884582",
//        "section_id": "7433032055723868198"
//        }

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouZiCompletionEventResponse implements Serializable {
    /**
     * 事件
     */
    @JsonProperty("id")
    private String ID;
    /**
     * 消息
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    /**
     * 已完成
     */
    @JsonProperty("bot_id")
    private String botId;
    /**
     * 指数
     */
    @JsonProperty("role")
    private String role;

    /**
     * 序列id
     */
    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;


    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("chat_id")
    private String chatId;


    @JsonProperty("section_id")
    private String sectionId;

    @JsonProperty("created_at")
    private Long createdAt;
    @JsonProperty("updated_at")
    private Long updatedAt;


}
