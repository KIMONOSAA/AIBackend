package com.kimo.listener;

import cn.hutool.json.JSONUtil;
import com.kimo.config.WebSocketHandler;
import com.kimo.domain.CouZiCompletionEventResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Mr.kimo
 * @title CouZiEventSourceListener
 * @date 2024/11/17 11:45
 * @description TODO
 */
@Slf4j
public class CouZiEventSourceListener extends EventSourceListener {


    private final Long userId;
    private final WebSocketHandler webSocketHandler;
    private final List<String> answerContents;
    private final CountDownLatch countDownLatch;

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }


    public List<String> getAnswerContents() {
        return answerContents;
    }

    public CouZiEventSourceListener(Long userId, WebSocketHandler webSocketHandler, List<String> answerContents, CountDownLatch countDownLatch) {
        this.userId = userId;
        this.webSocketHandler = webSocketHandler;
        this.answerContents = answerContents;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
        try {
            CouZiCompletionEventResponse response = JSONUtil.toBean(data, CouZiCompletionEventResponse.class);

            if (StringUtils.isNotBlank(response.getContent()) && "answer".equals(response.getType()) && response.getCreatedAt() == null) {
                // 存储 content
                webSocketHandler.sendMessageToUser(userId.toString(), response.getContent());
            }
            if (StringUtils.isNotBlank(response.getContent()) && "answer".equals(response.getType())) {
                // 存储 content
                answerContents.add(response.getContent());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClosed(EventSource eventSource) {
        log.info("对话完成");
        countDownLatch.countDown();
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        countDownLatch.countDown();
        // log.error("错误: {}, {}", response.code(), t.getMessage());
    }
}
