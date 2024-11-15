
package com.kimo.session.defaults;


import cn.hutool.http.ContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.ICoZeApi;
import com.kimo.common.Constants;
import com.kimo.common.ErrorCode;
import com.kimo.config.OkHttpClientProvider;
import com.kimo.domain.CouZiCompletionFileRequest;
import com.kimo.domain.CouZiCompletionFileResponse;
import com.kimo.domain.CouZiCompletionRequest;

import com.kimo.domain.CouZiCompletionResponse;
import com.kimo.exception.BusinessException;
import com.kimo.session.CoZeConfiguration;
import com.kimo.session.CoZeSession;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.kimo.constants.CouZiConstant.AUTHORIZATION;
import static com.kimo.constants.CouZiConstant.BEARER;



@Slf4j
@Service
public class DefaultCoZeSession implements CoZeSession {


    /**
     * 默认配置信息
     */
    private final CoZeConfiguration cozeConfiguration;

    /**
     * 工厂事件
     */
    private final EventSource.Factory factory;





    public DefaultCoZeSession(CoZeConfiguration cozeConfiguration) {
        this.cozeConfiguration = cozeConfiguration;
        this.factory = cozeConfiguration.createRequestFactory();
    }


    /**
     * 聊天完成
     *
     * @param cozeCompletionRequest coze完成请求
     * @param eventSourceListener   事件源侦听器
     * @return {@link EventSource}
     * @throws JsonProcessingException json处理异常
     */
    @Override
    public EventSource chatCompletions(CouZiCompletionRequest cozeCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException {
        return chatCompletions(Constants.NULL, Constants.NULL, cozeCompletionRequest, eventSourceListener);
    }


    /**
     * 问答模型扣子智能体AI
     *
     * @param apiHostByUser         用户提供api主机
     * @param apiKeyByUser          用户提供api密钥
     * @param cozeCompletionRequest coze完成请求
     * @param eventSourceListener   事件源侦听器
     * @return {@link EventSource}
     * @throws JsonProcessingException json处理异常
     */
    @Override
    public EventSource chatCompletions(String apiHostByUser, String apiKeyByUser, CouZiCompletionRequest cozeCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException {
        // 当前为流式模式，如果为false则抛出异常
        Assert.isTrue(cozeCompletionRequest.getStream(), "illegal parameter stream is false!");

        if (!StringUtils.isNotBlank(apiHostByUser)){
            apiHostByUser = "pat_7gwklsLnL5KGDMGecF6IuLazLWBNDqwyELV7nGUGrD215fi1D2yjWSKkzSSiVijO";
        }
        // 1. 先判断用户传递的 Host、Key 是否为空，为空则使用默认配置信息
        // 动态设置 Host、Key，便于用户传递自己的信息

        String apiUrl = "https://api.coze.cn/";
        // 2. 构建请求信息，并设置请求头 Authorization

        Request request = new Request.Builder()
                // 通过 IYuanQiApi 配置的 POST 接口，用这样的方式从统一的地方获取配置信息
                .url(apiUrl.concat(ICoZeApi.v1_chat_completions))
                .addHeader(AUTHORIZATION, BEARER + apiHostByUser)
                // 封装请求参数信息
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), new ObjectMapper()
                        .writeValueAsString(cozeCompletionRequest)))
                .build();

        // 返回结果信息；EventSource 对象可以取消应答

        return factory.newEventSource(request, eventSourceListener);
    }

    /**
     * 问答模型扣子智能体AI
     *
     * @param apiHostByUser         用户提供api主机
     * @param apiKeyByUser          用户提供api密钥
     * @param cozeCompletionRequest coze完成请求
     * @param eventSourceListener   事件源侦听器
     * @return {@link EventSource}
     * @throws JsonProcessingException json处理异常
     */
    @Override
    public EventSource chatCompletions(String apiHostByUser, String apiKeyByUser, CouZiCompletionFileRequest cozeCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException {
        // 当前为流式模式，如果为false则抛出异常
        Assert.isTrue(cozeCompletionRequest.getStream(), "illegal parameter stream is false!");

        if (!StringUtils.isNotBlank(apiHostByUser)){
            apiHostByUser = "pat_7gwklsLnL5KGDMGecF6IuLazLWBNDqwyELV7nGUGrD215fi1D2yjWSKkzSSiVijO";
        }
        // 1. 先判断用户传递的 Host、Key 是否为空，为空则使用默认配置信息
        // 动态设置 Host、Key，便于用户传递自己的信息

        String apiUrl = "https://api.coze.cn/";
        // 2. 构建请求信息，并设置请求头 Authorization
        Request request = new Request.Builder()
                // 通过 IYuanQiApi 配置的 POST 接口，用这样的方式从统一的地方获取配置信息
                .url(apiUrl.concat(ICoZeApi.v1_chat_completions))
                .addHeader(AUTHORIZATION, BEARER + apiHostByUser)
                // 封装请求参数信息
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), new ObjectMapper()
                        .writeValueAsString(cozeCompletionRequest)))
                .build();

        // 返回结果信息；EventSource 对象可以取消应答

        return factory.newEventSource(request, eventSourceListener);
    }

    /**
     * 问答模型扣子智能体AI
     *
     * @param apiHostByUser         用户提供api主机
     * @param apiKeyByUser          用户提供api密钥
     * @return {@link EventSource}
     * @throws JsonProcessingException json处理异常
     */
    @Override
    public CouZiCompletionFileResponse chatCompletions(String apiHostByUser, String apiKeyByUser, MultipartFile file) throws JsonProcessingException {
        // 当前为流式模式，如果为false则抛出异常
        // 1. 创建 OkHttpClient 实例
        OkHttpClient client = OkHttpClientProvider.getClient();
        if (!StringUtils.isNotBlank(apiHostByUser)){
            apiHostByUser = "pat_M6W3gFhKK9qwkj6IceAhBS29nSKarYfoWd1C6iDtUOD0Knv2nYXoMxs72TNrJ55Y";
        }
        // 1. 先判断用户传递的 Host、Key 是否为空，为空则使用默认配置信息
        // 动态设置 Host、Key，便于用户传递自己的信息

        String apiUrl = "https://api.coze.cn/";
        // 2. 构建 MultipartBody，用于上传文件
        RequestBody fileBody = null;
        try {
            fileBody = RequestBody.create(
                    MediaType.parse(file.getContentType()), // 获取文件的 MIME 类型
                    file.getBytes() // 文件内容
            );
        }catch (IOException e){
                throw new BusinessException(ErrorCode.GENERATING_THE_QR_CODE_ERROR);
        }


        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // 你可以修改 "file" 为你的文件参数名称
                .addFormDataPart("file", file.getOriginalFilename(), fileBody)
                .build();

        // 3. 构建请求信息，并设置请求头 Authorization
        Request request = new Request.Builder()
                // 通过 ICoZeApi 配置的 POST 接口，用这样的方式从统一的地方获取配置信息
                .url(apiUrl.concat(ICoZeApi.v1_chat_pic))
                .addHeader(AUTHORIZATION, BEARER + apiHostByUser)
                // 封装请求参数信息为 multipart
                .post(requestBody)
                .build();

        // 返回结果信息；EventSource 对象可以取消应答
        // 5. 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            CouZiCompletionFileResponse result = null;
            try {
                // 将 JSON 字符串转换为 ApiResponse 对象
                result = objectMapper.readValue(response.body().string(), CouZiCompletionFileResponse.class);

            } catch (Exception e) {
                e.printStackTrace();
            }
            // 6. 返回响应内容
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//
//    /**
//     * 工作流
//     *
//     * @param cozeWorkflowRequest 工作流请求
//     * @return 应答结果
//     */
//    @Override
//    public CoZeWorkFlowResponse workflowCompletions(CoZeWorkFlowRequest cozeWorkflowRequest)  {
//        return this.cozeApi.workflowCompletions(cozeWorkflowRequest).blockingGet();
//    }



}
