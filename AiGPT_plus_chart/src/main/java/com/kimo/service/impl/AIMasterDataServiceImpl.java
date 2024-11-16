package com.kimo.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.common.ErrorCode;
import com.kimo.config.WebSocketHandler;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.domain.CouZiCompletionEventResponse;
import com.kimo.domain.CouZiCompletionRequest;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.AIMasterdataMapper;
import com.kimo.mapper.PointMapper;

import com.kimo.model.dto.chart.AIMasterDataQueryRequest;
import com.kimo.model.dto.chart.UserDto;
import com.kimo.model.dto.po.AIMasterData;
import com.kimo.model.dto.po.Point;
import com.kimo.service.AIMasterDataService;
import com.kimo.service.AIMessageSessionService;
import com.kimo.session.CoZeConfiguration;
import com.kimo.session.CoZeSession;
import com.kimo.session.defaults.DefaultCoZeSessionFactory;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.kimo.constant.Constants.PointNumber;
import static com.kimo.constants.CouZiConstant.BEARER;


/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class AIMasterDataServiceImpl extends ServiceImpl<AIMasterdataMapper, AIMasterData>
    implements AIMasterDataService {

    @Autowired
    private UserClient userClient;


//    @Autowired
//    private Executor executor;

    @Autowired
    private CoZeSession coZeSession;

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private AIMessageSessionService aiMessageSessionService;

    @Autowired
    private WebSocketHandler webSocketHandler;

//    @Override
//    @Transactional
//    public boolean validAiMasterData(AIMasterData aiMasterData, HttpServletRequest request) throws Exception {
//        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
//        UserDto userDto = userClient.GobalGetLoginUser(username);
//        ThrowUtils.throwIf(userDto == null, ErrorCode.SYSTEM_ERROR);
//        aiMasterData.setUserId(userDto.getId());
////        this.increase(userDto);
//        CompletionRequest aiMasterDataRequest = CompletionRequest.builder()
//                .stream(true)
//                .messages(Collections.singletonList(Text.builder().role(CompletionRequest.Role.USER.getCode()).content(aiMasterData.getUserTitle()).build()))
//                .model(CompletionRequest.Model.XUNFEI.getCode())
//                .build();
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//
//        XunFeiEventSourceListener xunFeiEventSourceListener = new XunFeiEventSourceListener(countDownLatch);
//
//        EventSource eventSource = executor.completions(aiMasterDataRequest,null, xunFeiEventSourceListener);
//        xunFeiEventSourceListener.getCountDownLatch().await();
//        log.info(eventSource.toString());
//        aiMasterData.setAiBody(xunFeiEventSourceListener.getAnswer());
//        return true;
//
//    }


    @Override
    public String getChartDataForCouZiChart(AIMasterData chartData, HttpServletRequest request, String botId, String user, String token) throws Exception {

        UserDto userDtoForRedisOrLock = aiMessageSessionService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        chartData.setUserId(userId);

        aiMessageSessionService.fetchUpdatePoint(PointNumber,userId);


        List<String> answerContents = new ArrayList<>();
        String lastAnswerContent = null;
        // 1. 创建参数
        ArrayList<GouZiAdditionalMessages> goZeAdditionalMessages1 = new ArrayList<>();
        GouZiAdditionalMessages goZeAdditionalMessages = new GouZiAdditionalMessages();
        goZeAdditionalMessages.setContent(chartData.getUserTitle());
        goZeAdditionalMessages.setRole("user");
        goZeAdditionalMessages.setContent_type("text");
        goZeAdditionalMessages1.add(goZeAdditionalMessages);
        // 1. 创建参数
        CouZiCompletionRequest chatCompletion = CouZiCompletionRequest
                .builder()
                .stream(true)
                .userId(user)
                .chatHistory(true)
                .botId(botId)
                .AdditionalMessages(goZeAdditionalMessages1)
                .build();
        // 2. 发起请求
        // 1. 配置文件
        CoZeConfiguration yuanQiConfiguration = new CoZeConfiguration();
        yuanQiConfiguration.setApiHost("https://api.coze.cn/");
        yuanQiConfiguration.setApiKey(BEARER + token);
        yuanQiConfiguration.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        // 2. 会话工厂
        DefaultCoZeSessionFactory factory = new DefaultCoZeSessionFactory(yuanQiConfiguration);
        // 3. 开启会话
        this.coZeSession = factory.openSession();
        log.info("openAiSession:{}", coZeSession);
        // 2. 请求等待
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventSource eventSource = coZeSession.chatCompletions(token,null,chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
                try {
                    CouZiCompletionEventResponse response = JSONUtil.toBean(data, CouZiCompletionEventResponse.class);

//                    couZiCompletionEventResponses.add(response.getContent());
                    if (StringUtils.isNotBlank(response.getContent()) && "answer".equals(response.getType())) {
                        // 存储 content
                        webSocketHandler.sendMessageToUser(userId.toString(), response.getContent());
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
            lastAnswerContent = answerContents.get(answerContents.size() - 1);
        }
        return lastAnswerContent;
    }


    @Override
    public Wrapper<AIMasterData> getQueryWrapper(AIMasterDataQueryRequest aiMasterDataQueryRequest, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = aiMessageSessionService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();

        QueryWrapper<AIMasterData> queryWrapper = new QueryWrapper<>();
        if (aiMasterDataQueryRequest == null) {
            return queryWrapper;
        }
        if(aiMasterDataQueryRequest.getAiMessageId() > 0){
            queryWrapper.eq(SqlConstants.AI_MESSAGE_SESSION_ID,aiMasterDataQueryRequest.getAiMessageId());
        }
        queryWrapper.eq(SqlConstants.USER_ID,userId);
        String sortField = aiMasterDataQueryRequest.getSortField();
        String sortOrder = aiMasterDataQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    private synchronized void increase(UserDto user){
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.USER_ID,user.getId());
        Point point = pointMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(point == null,ErrorCode.OPERATION_ERROR);
        ThrowUtils.throwIf(point.getPoint() < 5,ErrorCode.OPERATION_ERROR);
        point.setPoint(point.getPoint() - 5);
        pointMapper.updateById(point);
    }

}




