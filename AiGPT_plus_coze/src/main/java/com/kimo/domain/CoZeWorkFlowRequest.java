
package com.kimo.domain;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


@Data
@Builder
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class CoZeWorkFlowRequest implements Serializable {
    
    /**
     * 工作流ID
     * 必选
     */
    @JsonProperty("workflow_id")
    private String workflowId;
    
    /**
     * 工作流开始节点的输入参数及取值，你可以在指定工作流的编排页面查看参数列表。
     * 可选
     */
    @JsonProperty("parameters")
    private JSONObject parameters;
    
    /**
     * 需要关联的 Bot ID
     * 可选
     */
    @JsonProperty("bot_id")
    private String botId;

    /**
     * 用于指定一些额外的字段，以 Map[String][String] 格式传入。例如某些插件 会隐式用到的经纬度等字段。
     * 目前仅支持以下字段：
     * latitude：String 类型，表示经度。
     * longitude：String 类型，表示纬度。
     * user_id：Integer 类型，表示用户 ID。
     * 可选
     */
    @JsonProperty("ext")
    private HashMap<String,String> ext;
    
    /**
     * 是否异步运行。异步运行时可通过 execute_id 调用【草稿】查询工作流执行结果 API 获取工作流的执行结果。
     * true：异步运行。
     * false：（默认）同步运行。
     * Optional
     */
    @JsonProperty("is_async")
    private Boolean isAsync;
    
    
}
