package com.kimo.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kimo.common.ErrorCode;
import com.kimo.config.WebSocketHandler;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.domain.CouZiCompletionEventResponse;
import com.kimo.domain.CouZiCompletionRequest;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.listener.CouZiEventSourceListener;
import com.kimo.mapper.AIMasterdataMapper;
import com.kimo.mapper.AIMessageSessionMapper;
import com.kimo.mapper.PointMapper;

import com.kimo.model.dto.chart.AIMasterDataQueryRequest;
import com.kimo.model.dto.chart.UserDto;
import com.kimo.model.dto.po.AIMasterData;
import com.kimo.model.dto.po.AIMessageSession;
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
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.kimo.constans.CouZiConstant.COU_ZI_ZPI_HOST;
import static com.kimo.constant.Constants.PointNumber;
import static com.kimo.constants.CouZiConstant.BEARER;
import static com.kimo.utils.YouBanUtils.*;


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
    private AIMessageSessionMapper  aiMessageSessionMapper;

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private AIMasterdataMapper masterdataMapper;



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 12:03 
     * @return: java.util.Map<java.lang.String,java.lang.String>
     * @Param: [com.kimo.model.dto.po.AIMasterData, jakarta.servlet.http.HttpServletRequest, java.lang.String, java.lang.String, java.lang.String]
     * @Description: 调用扣子进行AI问答并实现实时通信
     */
    public Map<String, String> getChartDataForCouZiChart(AIMasterData chartData, HttpServletRequest request, String botId, String user, String token) throws Exception {
        // 更具描述性的变量名
        HashMap<String, String> resultMap = new HashMap<>();
        extractedCouZiResults(chartData, request, botId, user, token, resultMap);
        return resultMap;
    }


    private void extractedCouZiResults(AIMasterData chartData, HttpServletRequest request, String botId, String user, String token, HashMap<String, String> resultMap) throws JsonProcessingException, InterruptedException {
        // Redis获取用户数据
        UserDto userDtoForRedisOrLock = aiMessageSessionService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null, ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        chartData.setUserId(userId);
        resultMap.put("userId", String.valueOf(userId));

        //更新积分
        aiMessageSessionService.fetchUpdatePoint(PointNumber, userId);

        List<String> answerContents = new ArrayList<>();

        //获取最后AI返回的所有数据
        String lastAnswerContent = null;

        //创建请求参数
        GouZiAdditionalMessages goZeAdditionalMessages = createGouZiAdditionalMessages(chartData);
        ArrayList<GouZiAdditionalMessages> goZeAdditionalMessages1 = new ArrayList<>();
        goZeAdditionalMessages1.add(goZeAdditionalMessages);

        // 创建并配置请求
        CouZiCompletionRequest couZiCompletionRequest = CreateCouZiCompletionRequest(true, user, true, botId, goZeAdditionalMessages1);
        CoZeConfiguration yuanQiConfiguration = new CoZeConfiguration();
        configExtracted(token, yuanQiConfiguration);

        // 创建会话工厂
        DefaultCoZeSessionFactory factory = new DefaultCoZeSessionFactory(yuanQiConfiguration);
        this.coZeSession = factory.openSession();
        log.info("openAiSession:{}", coZeSession);

        // 使用 CountDownLatch 等待异步结果
        CountDownLatch countDownLatch = new CountDownLatch(1);

        //调用AI
        CouZiEventSourceListener couZiEventSourceListener = new CouZiEventSourceListener(userId, webSocketHandler, answerContents, countDownLatch);

        EventSource eventSource = coZeSession.chatCompletions(token, null, couZiCompletionRequest, couZiEventSourceListener);
        couZiEventSourceListener.getCountDownLatch().await();

        // 获取最终的回答内容
        if (!couZiEventSourceListener.getAnswerContents().isEmpty()) {
            lastAnswerContent = couZiEventSourceListener.getAnswerContents().get(couZiEventSourceListener.getAnswerContents().size() - 1);
        }
        resultMap.put("aiData", lastAnswerContent);
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 12:06
     * @return: void
     * @Param: [java.util.Map<java.lang.String,java.lang.String>, java.lang.String]
     * @Description: 是否有创建会话，没有就创建给，返回true，然后创建AI问答
     */
    public void IsAiMessagesessionForCourse(Map<String,String> map,String data) {
        Boolean isBooleanAiMessageSession = IsBooleanAiMessageSession(map,data);
        ThrowUtils.throwIf(!isBooleanAiMessageSession,ErrorCode.ADD_DATABASE_ERROR);
        createAiMasterData(map, data,null);
    }

    /**
     * @Author: Mr.kimo
     * @Date: 12:07
     * @return:
     * @Param:
     * @Description: 创建AI问答的数据信息
     */
    private AIMasterData createAiMasterData(Map<String,String> map, String data, MultipartFile file){
        long userId = Long.parseLong(map.get("userId"));
        QueryWrapper<AIMessageSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        AIMessageSession aiMessageSessionId = aiMessageSessionService.getOne(queryWrapper);
        AIMasterData aiMasterData = new AIMasterData();
        aiMasterData.setUserId(userId);
        aiMasterData.setCreateTime(LocalDateTime.now());
        aiMasterData.setUpdateTime(LocalDateTime.now());
        aiMasterData.setAiBody(map.get("aiData"));
        aiMasterData.setUserBody(data);
        aiMasterData.setAiMessageSessionId(aiMessageSessionId.getId());
        if(file != null){
            Blob avatarPhoyo = null;
            byte[] avatarBytes = null;
            try {
                if(file != null && !file.isEmpty()){
                    byte[] bytes = file.getBytes();
                    avatarPhoyo = new SerialBlob(bytes);
                }
                if (avatarPhoyo != null) {
                    avatarBytes = avatarPhoyo.getBytes(1, (int) avatarPhoyo.length()); // Get the entire blob as a byte array
                }
                aiMasterData.setPic(avatarBytes);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }

        }
        int insert = masterdataMapper.insert(aiMasterData);
        return aiMasterData;
    }


    /**
     * @Author: Mr.kimo
     * @Date: 12:07
     * @return:
     * @Param:
     * @Description: 判断是否有会话
     */
    private Boolean IsBooleanAiMessageSession(Map<String,String> map,String title){
        long userId = Long.parseLong(map.get("userId"));
        QueryWrapper<AIMessageSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);

        //获取用户会话（一个用户只有一个会话）
        AIMessageSession aiMessageSessionId = aiMessageSessionMapper.selectOne(queryWrapper);

        //为空就创建
        if(aiMessageSessionId == null){
            AIMessageSession aiMessageSession1 = new AIMessageSession();
            aiMessageSession1.setUserId(userId);
            aiMessageSession1.setTitle(title);
            aiMessageSession1.setCreateTime(LocalDateTime.now());
            aiMessageSession1.setUpdateTime(LocalDateTime.now());
            boolean save = aiMessageSessionService.save(aiMessageSession1);
            ThrowUtils.throwIf(!save,ErrorCode.ADD_DATABASE_ERROR);
        }
        return true;
    }



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 12:08
     * @return: com.baomidou.mybatisplus.core.conditions.Wrapper<com.kimo.model.dto.po.AIMasterData>
     * @Param: [com.kimo.model.dto.chart.AIMasterDataQueryRequest, jakarta.servlet.http.HttpServletRequest]
     * @Description: 分页列出所以用户问答数据
     */
    public Wrapper<AIMasterData> getQueryWrapper(AIMasterDataQueryRequest aiMasterDataQueryRequest, HttpServletRequest request) {
        //redis获取用户信息
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

}




