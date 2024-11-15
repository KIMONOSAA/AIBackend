
package com.kimo.session;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.kimo.domain.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.multipart.MultipartFile;


public interface CoZeSession {
    

    
    /**
     * 简单问答 - 流式
     *
     * @param cozeCompletionRequest coze完成请求
     * @param eventSourceListener   事件源侦听器
     * @return {@link EventSource}
     * @throws JsonProcessingException json处理异常
     */
    EventSource chatCompletions(CouZiCompletionRequest cozeCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException;


    /**
     * 简单问答 - 流式 - 自定义apiHost和apiKey
     *
     * @param apiHostByUser         用户提供api主机
     * @param apiKeyByUser          用户提供api密钥
     * @param cozeCompletionRequest coze完成请求
     * @param eventSourceListener   事件源侦听器
     * @return {@link EventSource}
     * @throws JsonProcessingException json处理异常
     */
    EventSource chatCompletions(String apiHostByUser, String apiKeyByUser, CouZiCompletionRequest cozeCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException;


    EventSource chatCompletions(String apiHostByUser, String apiKeyByUser, CouZiCompletionFileRequest cozeCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException;


    CouZiCompletionFileResponse chatCompletions(String apiHostByUser, String apiKeyByUser, MultipartFile file) throws JsonProcessingException;
    //
//    /**
//     * 工作流
//     *
//     * @param cozeWorkflowRequest 工作流请求
//     * @return {@link CouZiCompletionResponse}
//     */
//    CoZeWorkFlowResponse workflowCompletions(CoZeWorkFlowRequest cozeWorkflowRequest) throws JsonProcessingException;
}
