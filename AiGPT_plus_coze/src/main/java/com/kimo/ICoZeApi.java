
package com.kimo;

import com.kimo.domain.CouZiCompletionRequest;
import com.kimo.domain.CouZiCompletionResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ICoZeApi {

    /**
     * v1聊天完成
     */
    String v1_chat_completions = "v3/chat";

    /**
     * v1工作流
     */
    String v1_workflow_completions = "v1/workflow/run";

    /**
     * 问答智能体
     *
     * @param CoZeCompletionRequest 请求信息
     * @return 应答结果
     */
    @POST(v1_chat_completions)
    Single<CouZiCompletionResponse> completions(@Body CouZiCompletionRequest CoZeCompletionRequest);


}
