package test;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kimo.domain.*;
import com.kimo.session.CoZeConfiguration;
import com.kimo.session.CoZeSession;


import com.kimo.session.defaults.DefaultCoZeSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;



@Slf4j
public class CozeAPI {

    private CoZeSession coZeSession;

    /**
     * 测试会话工厂
     */
    @Before
    public void test_session_Factory() {
        // 1. 配置文件
        CoZeConfiguration yuanQiConfiguration = new CoZeConfiguration();
        yuanQiConfiguration.setApiHost("https://api.coze.cn/");
        yuanQiConfiguration.setApiKey("Bearer pat_7gwklsLnL5KGDMGecF6IuLazLWBNDqwyELV7nGUGrD215fi1D2yjWSKkzSSiVijO");
        yuanQiConfiguration.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        // 2. 会话工厂
        DefaultCoZeSessionFactory factory = new DefaultCoZeSessionFactory(yuanQiConfiguration);
        // 3. 开启会话
        this.coZeSession = factory.openSession();
        log.info("openAiSession:{}", coZeSession);
    }



    @Test
    public void test_chat_completions_stream() throws JsonProcessingException, InterruptedException {
        ArrayList<CouZiCompletionEventResponse> couZiCompletionEventResponses = new ArrayList<>();
        List<String> answerContents = new ArrayList<>();
        // 1. 创建参数
        ArrayList<GouZiAdditionalMessages> goZeAdditionalMessages1 = new ArrayList<>();
        GouZiAdditionalMessages goZeAdditionalMessages = new GouZiAdditionalMessages();
        goZeAdditionalMessages.setContent("分析需求:分析网站用户成绩准确率信息，原始数据:科目,成绩语文,60数学,20英语,30，请使用饼图");
        goZeAdditionalMessages.setRole("user");
        goZeAdditionalMessages.setContent_type("text");
        goZeAdditionalMessages1.add(goZeAdditionalMessages);
        // 1. 创建参数
        CouZiCompletionRequest chatCompletion = CouZiCompletionRequest
                .builder()
                .stream(true)
                .userId("user_001")
                .chatHistory(true)
                .botId("7432966743104520192")
                .AdditionalMessages(goZeAdditionalMessages1)
                .build();
        // 2. 发起请求
        // 2. 请求等待
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventSource eventSource = coZeSession.chatCompletions(null,null,chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
                try {
                    CouZiCompletionEventResponse response = JSONUtil.toBean(data, CouZiCompletionEventResponse.class);


                        log.info("返回内容: {}", response.toString());
//                    couZiCompletionEventResponses.add(response.getContent());
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
//                log.error("错误: {}, {}", response.code(), t.getMessage());
            }
        });
        countDownLatch.await();
        if (!answerContents.isEmpty()) {
            String lastAnswerContent = answerContents.get(answerContents.size() - 1);
            // 这里可以使用 lastAnswerContent，例如输出
            System.out.println("最后的内容: " + lastAnswerContent);
        }
    }



}
