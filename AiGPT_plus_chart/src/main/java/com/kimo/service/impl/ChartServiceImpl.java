package com.kimo.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kimo.amqp.ChartProducer;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.config.WebSocketHandler;
import com.kimo.constant.ChartConstant;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.domain.*;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;

import com.kimo.listener.CouZiEventSourceListener;
import com.kimo.mapper.AIMasterdataMapper;
import com.kimo.mapper.AIMessageSessionMapper;
import com.kimo.mapper.ChartMapper;


import com.kimo.model.dto.chart.ChartEditRequest;
import com.kimo.model.dto.chart.ChartQueryRequest;
import com.kimo.model.dto.chart.GenChartyByAIRequest;
import com.kimo.model.dto.po.AIMasterData;
import com.kimo.model.dto.po.AIMessageSession;
import com.kimo.model.dto.po.Chart;

import com.kimo.model.dto.vo.BiResponse;
import com.kimo.service.AIMessageSessionService;
import com.kimo.service.ChartService;
import com.kimo.session.CoZeConfiguration;
import com.kimo.session.CoZeSession;
import com.kimo.session.defaults.DefaultCoZeSessionFactory;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.kimo.constans.CouZiConstant.*;
import static com.kimo.constant.Constants.PointNumber;

import static com.kimo.utils.ExcelUtils.excelToCsv;
import static com.kimo.utils.YouBanUtils.*;

/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{


    @Autowired
    private UserClient userClient;

    @Autowired
    private ChartMapper chartMapper;

    @Resource
    private ChartProducer chartProducer;

    @Autowired
    private AIMessageSessionMapper aiMessageSessionMapper;

    @Autowired
    private AIMasterdataMapper masterdataMapper;

//    @Resource
//    private PointMapper pointMapper;

    @Autowired
    private CoZeSession coZeSession;


    @Autowired
    private ServletUtils  servletUtils;

    @Autowired
    private AIMessageSessionService aiMessageSessionService;

    @Autowired
    private AIMasterdataMapper  aiMasterdataMapper;

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Override
    public Long getLoginUser(HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = aiMessageSessionService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        ThrowUtils.throwIf(userId <= 0,ErrorCode.OPERATION_ERROR);
        return userId;
    }


    private Map<String, String> getLoginUserForUserName(HttpServletRequest request) {
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        UserDto userDtoForRedisOrLock = aiMessageSessionService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        ThrowUtils.throwIf(userId <= 0,ErrorCode.OPERATION_ERROR);
        objectObjectHashMap.put("userId", String.valueOf(userId));
        objectObjectHashMap.put("userName", userDtoForRedisOrLock.getUserName());
        return objectObjectHashMap;
    }

    @Override
    public String getChartDataForCouZiChartAndFileData(GouZiAdditionalMessages chartData, CouZiAdditionalFileMessage fileData, String botId, String user, String userId, String token) throws Exception {
        try {
            if (chartData != null) {
                return executeChatCompletion(chartData, null, botId, user, userId, token);
            } else {
                return executeChatCompletion(null, fileData, botId, user, userId, token);
            }
        } catch (Exception e) {
            // 统一异常处理，记录日志或其他操作
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"处理数据时出错");
        }
    }

    private String executeChatCompletion(GouZiAdditionalMessages chartData, CouZiAdditionalFileMessage fileData, String botId, String user, String userId, String token) throws Exception {
        List<String> answerContents = new ArrayList<>();

        // 减去积分
        if(!"7433801236367556627".equals(botId)){
            aiMessageSessionService.fetchUpdatePoint(PointNumber, Long.parseLong(userId));
        }

        // 创建请求对象
        Object requestObject = createCompletionRequest(chartData, fileData, user, botId);

        // 初始化会话和监听器
        CoZeConfiguration configuration = new CoZeConfiguration();
        configExtracted(token, configuration);
        DefaultCoZeSessionFactory factory = new DefaultCoZeSessionFactory(configuration);
        this.coZeSession = factory.openSession();
        CouZiEventSourceListener listener = initializeListener(Long.parseLong(userId), answerContents);

        // 执行请求
        if (requestObject instanceof CouZiCompletionRequest) {
            coZeSession.chatCompletions(null, null, (CouZiCompletionRequest) requestObject, listener);
        } else if (requestObject instanceof CouZiCompletionFileRequest) {
            coZeSession.chatCompletions(null, null, (CouZiCompletionFileRequest) requestObject, listener);
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请求对象类型无效");
        }

        // 等待结果
        listener.getCountDownLatch().await();
        return getLastAnswerContent(listener);
    }

    private CouZiEventSourceListener initializeListener(Long userId, List<String> answerContents) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        return new CouZiEventSourceListener(userId, webSocketHandler, answerContents, countDownLatch);
    }

    private String getLastAnswerContent(CouZiEventSourceListener listener) {
        if (!listener.getAnswerContents().isEmpty()) {
            return listener.getAnswerContents().get(listener.getAnswerContents().size() - 1);
        }
        return null;
    }
    // 创建请求对象的通用方法
    private Object createCompletionRequest(GouZiAdditionalMessages chartData, CouZiAdditionalFileMessage fileData, String user, String botId) {
        if (chartData != null) {
            ArrayList<GouZiAdditionalMessages> goZeAdditionalMessages = new ArrayList<>();
            goZeAdditionalMessages.add(chartData);
            return CreateCouZiCompletionRequest(true, user, true, botId, goZeAdditionalMessages);
        } else if (fileData != null) {
            ArrayList<CouZiAdditionalFileMessage> goZiAdditionalFileMessage = new ArrayList<>();
            goZiAdditionalFileMessage.add(fileData);
            return CreateCouZiCompletionFileRequest(true, user, true, botId, goZiAdditionalFileMessage);
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"必须提供 chartData 或 fileData");
    }



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:46
     * @return: java.lang.Boolean
     * @Param: [jakarta.servlet.http.HttpServletRequest]
     * @Description: 删除图表，版本更新，已弃用
     */
    public Boolean deletedChart(HttpServletRequest request) {
        Long loginUser = this.getLoginUser(request);
        // 判断是否存在
        Chart oldChart = chartMapper.selectById(loginUser);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(loginUser) && !userClient.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        int id1 = chartMapper.deleteById(loginUser);
        return id1 > 0;
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:46
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.kimo.model.dto.po.Chart>
     * @Param: [jakarta.servlet.http.HttpServletRequest, com.kimo.model.dto.chart.ChartQueryRequest]
     * @Description: 版本更新，已弃用
     */
    public Page<Chart> listMyChartByPage(HttpServletRequest request, ChartQueryRequest chartQueryRequest) {
        Long loginUser = this.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser);
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:47
     * @return: java.lang.Boolean
     * @Param: [jakarta.servlet.http.HttpServletRequest, com.kimo.model.dto.chart.ChartEditRequest]
     * @Description: 版本更新，已弃用
     */
    public Boolean editChart(HttpServletRequest request, ChartEditRequest chartEditRequest) {
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        Long loginUser = this.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartMapper.selectById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser) && !userClient.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        int updateById = chartMapper.updateById(chart);
        return updateById > 0;
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 17:47
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.kimo.model.dto.po.Chart>
     * @Param: [long, long, com.kimo.model.dto.chart.ChartQueryRequest]
     * @Description: 版本更新，已弃用
     */
    public Page<Chart> listChartUserByPage(long current, long size, ChartQueryRequest chartQueryRequest) {
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
    }

    @Override
    @Transactional
    public BiResponse genChartByAIRabbitMQ(MultipartFile multipartFile, GenChartyByAIRequest genChartByAI, HttpServletRequest request) {
        // 验证文件
        validateFile(multipartFile);

        // 构造用户输入
        String userGoal = StringUtils.isNotBlank(genChartByAI.getChartType())
                ? genChartByAI.getGoal() + ChartConstant.UTILIZATION + genChartByAI.getChartType()
                : genChartByAI.getGoal();

        String userInput = String.join("\n",
                ChartConstant.ANALYZE_DEMAND,
                userGoal,
                ChartConstant.RAW_DATA,
                excelToCsv(multipartFile)
        );

        // 创建并插入 Chart 对象
        Chart chart = createChart(genChartByAI.getName(), genChartByAI.getGoal(), genChartByAI.getChartType(), userInput);
        int insert = chartMapper.insert(chart);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 发送消息到队列
        chartProducer.sendMessage(String.valueOf(chart.getId()));

        // 构造响应
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    private void validateFile(MultipartFile multipartFile) {
        final long ONE_MB = 1024L * 1024L;
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList(ChartConstant.SPECIFICATION_XLSX, ChartConstant.SPECIFICATION_XLS);

        // 文件大小校验
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");
        // 文件格式校验
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    private Chart createChart(String name, String goal, String chartType, String resultData) {
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);

        chart.setGenResult(resultData);
        return chart;

    }



    @Override
    @Transactional
    public Map<String, String> fetchCourseInfoForChart(GouZiAdditionalMessages additionalMessages, String botId, String token, String courseId, HttpServletRequest request) {
        // 获取用户信息
        Map<String, String> loginUserInfo = getUserInfoFromRequest(request);
        String userId = loginUserInfo.get("userId");
        String userName = loginUserInfo.get("userName");

        // 更新用户积分
        updateUserPoints(userId);

        // 获取 CouZi 数据
        String chartDataForCouZiChart;
        try {
            chartDataForCouZiChart = getChartDataForCouZiChartAndFileData(additionalMessages, null, botId, userName, userId, token);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FETCH_COUZI_ERROR, "获取 CouZi 的数据时出错");
        }

        // 构建返回数据
        Map<String, String> responseData = new HashMap<>();
        responseData.put("userId", userId);
        responseData.put("aiData", chartDataForCouZiChart);
        return responseData;
    }

    private Map<String, String> getUserInfoFromRequest(HttpServletRequest request) {
        return this.getLoginUserForUserName(request);
    }

    private void updateUserPoints(String userId) {
        aiMessageSessionService.fetchUpdatePoint(PointNumber, Long.parseLong(userId));
    }


    /**
     * 获取 CouZi 课程信息数据
     *
     * 该方法用于处理上传的文件，提取用户信息并通过 CoZe API 发送请求获取处理结果。
     *
     * @param file    用户上传的文件 (图片、文档等)
     * @param request HttpServletRequest 对象，用于获取当前请求的用户信息
     * @return 包含用户信息和响应数据的 Map
     * @throws BusinessException 如果调用 CoZe API 失败或处理异常
     */
    @Override
    public Map<String, String> getCourseInfoDataForCouZi(MultipartFile file, HttpServletRequest request) {

        // 1. 从 HttpServletRequest 中获取当前用户的信息
        // 使用辅助方法 getUserInfoFromRequest 提取用户信息，并存储在 loginUserInfo Map 中
        Map<String, String> loginUserInfo = this.getUserInfoFromRequest(request);
        String userId = loginUserInfo.get("userId");
        String userName = loginUserInfo.get("userName");

        // 2. 将用户 ID 添加回 loginUserInfo Map 中，确保其在后续处理中存在
        loginUserInfo.put("userId", userId);

//        // 3. 更新用户的积分（调用积分更新服务）
//        // 这一步可能是根据上传文件的处理行为来奖励用户积分
//        this.updateUserPoints(userId);

        // 4. 创建 CoZeConfiguration 配置对象
        // CoZeConfiguration 是与 CoZe API 交互的配置类
        // 使用 configExtracted 方法初始化配置，参数 AI_PIC_BOT_TOKEN 是 API 的授权令牌
        CoZeConfiguration configuration = new CoZeConfiguration();
        configExtracted(AI_PIC_BOT_TOKEN, configuration);

        // 5. 使用配置对象创建会话工厂 (Session Factory)
        // DefaultCoZeSessionFactory 负责基于配置初始化会话
        DefaultCoZeSessionFactory factory = new DefaultCoZeSessionFactory(configuration);

        // 6. 通过会话工厂开启一个新的会话
        // 这一步会生成一个新的 coZeSession 对象，用于后续的 API 请求
        this.coZeSession = factory.openSession();

        try {
            // 7. 调用 CoZe API 发送上传的文件数据
            // 使用 coZeSession.chatCompletions 方法与 AI 交互，获取 CouZiCompletionFileResponse 响应
            CouZiCompletionFileResponse couZiCompletionFileResponse = coZeSession.chatCompletions(AI_PIC_BOT_TOKEN, null, file);

            // 8. 从 API 响应中提取 picId，并将其存储在 loginUserInfo Map 中
            // 该 ID 可能用于后续处理或标识此次上传文件的唯一标识
            loginUserInfo.put("picId", couZiCompletionFileResponse.getData().getId());
        } catch (JsonProcessingException e) {
            // 9. 如果在 API 调用或 JSON 解析时发生异常，抛出自定义 BusinessException
            // 错误码为 FETCH_COUZI_ERROR，表示获取 CouZi 数据时出错
            throw new BusinessException(ErrorCode.FETCH_COUZI_ERROR);
        }

        // 10. 返回包含用户信息和 AI 响应数据的 Map
        return loginUserInfo;
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:06
     * @return: java.util.Map<java.lang.String,java.lang.String>
     * @Param: [com.kimo.domain.GouZiAdditionalMessages, java.lang.String, jakarta.servlet.http.HttpServletRequest]
     * @Description: 根据用户设置的时间已经当前课程描述和章节的描述调用Bot智能体生成学习计划
     */
    public Map<String,String> getLearnTeachPlanForCouZi(GouZiAdditionalMessages chartData, String courseId, HttpServletRequest request) {
        Map<String, String> loginUserForUserName = this.getLoginUserForUserName(request);
        String userId = loginUserForUserName.get("userId");
        String userName = loginUserForUserName.get("userName");
        loginUserForUserName.put("userId", userId);
        this.updateUserPoints(userId);
        String chartDataForCouZiChart = null;
        try {
            chartDataForCouZiChart = this.getChartDataForCouZiChartAndFileData(chartData,null, LEARN_TEACH_PLAN_BOT_ID, loginUserForUserName.get("userName"),loginUserForUserName.get("userId"), LEARN_TEACH_PLAN_TOKEN);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FETCH_COUZI_ERROR);
        }
        loginUserForUserName.put("aiData", chartDataForCouZiChart);
        return loginUserForUserName;
    }

    /**
     * @Author: Mr.kimo
     * @Date: 18:08
     * @return: true 如果会话创建成功，false 如果会话已存在
     * @Param: userId 用户ID，title 会话标题
     * @Description: 确保当前用户有 AI 消息会话，如果没有则创建
     */
    private Boolean ensureAiMessageSessionExists(Long userId, String title) {
        // 使用 QueryWrapper 查询是否已经存在 AIMessageSession
        QueryWrapper<AIMessageSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        AIMessageSession aiMessageSession = aiMessageSessionMapper.selectOne(queryWrapper);

        // 如果会话不存在，则创建新会话
        if (aiMessageSession == null) {
            AIMessageSession newSession = new AIMessageSession();
            newSession.setUserId(userId);
            newSession.setTitle(title);
            newSession.setCreateTime(LocalDateTime.now());
            newSession.setUpdateTime(LocalDateTime.now());

            // 保存新会话并检查是否成功
            boolean saveSuccess = aiMessageSessionService.save(newSession);
            ThrowUtils.throwIf(!saveSuccess, ErrorCode.ADD_DATABASE_ERROR);

            return true;  // 会话成功创建
        }

        // 如果会话已存在，则返回 false
        return true;
    }



    /**
     * @Author: Mr.kimo
     * @Date: 18:10
     * @return:
     * @Param:
     * @Description: 更新用户与Bot智能体对话数据
     */
    private Boolean updatedAiMasterData(Map<String,String> map,String data,MultipartFile file){
        //获取data所需要的数据
        long userId = Long.parseLong(map.get("userId"));
        long masterId = Long.parseLong(map.get("masterId"));
        long sessionId = Long.parseLong(map.get("sessionId"));
        String aiData = map.get("aiData");

        // 查询 AI Master 数据
        AIMasterData aiMasterData = aiMasterdataMapper.selectById(masterId);

        // 如果找不到数据，则抛出异常
        ThrowUtils.throwIf(aiMasterData == null, ErrorCode.NOT_FOUND_ERROR, "未找到 AI 主数据");

        // 校验会话 ID 和用户 ID 是否匹配
        ThrowUtils.throwIf(aiMasterData.getAiMessageSessionId() != sessionId, ErrorCode.INVALID_SESSION_ERROR);
        ThrowUtils.throwIf(aiMasterData.getUserId() != userId, ErrorCode.INVALID_USER_ERROR);

        // 更新 AI 数据体和时间
        aiMasterData.setAiBody(aiData);
        aiMasterData.setUserBody(data);
        aiMasterData.setUpdateTime(LocalDateTime.now()); // 或者 LocalDateTime.now(ZoneId.of("UTC"))

        // 执行更新操作
        int updatedRows = aiMasterdataMapper.updateById(aiMasterData);
        ThrowUtils.throwIf(updatedRows <= 0, ErrorCode.DATABASE_UPDATE_ERROR);

        return true;  // 返回更新成功
    }


    /**
     * @Author: Mr.kimo
     * @Date: 18:10
     * @return: 创建并保存的 AIMasterData 对象
     * @Param: map 包含用户相关信息的 map，data 用户提供的数据，file 上传的文件（头像等）
     * @Description: 创建并保存 AI Master 数据，处理文件和数据并存储
     */
    private AIMasterData createAndSaveAiMasterData(Map<String,String> map, String data, MultipartFile file) {
        long userId = Long.parseLong(map.get("userId"));

        // 查询 AI 会话 ID
        AIMessageSession aiMessageSession = aiMessageSessionService.getOne(new QueryWrapper<AIMessageSession>().eq("user_id", userId));
        ThrowUtils.throwIf(aiMessageSession == null, ErrorCode.NOT_FOUND_ERROR, "未找到用户的 AI 会话：" + userId);

        // 创建 AIMasterData 实体
        AIMasterData aiMasterData = new AIMasterData();
        aiMasterData.setUserId(userId);
        aiMasterData.setCreateTime(LocalDateTime.now()); // 可使用 UTC 时间：LocalDateTime.now(ZoneId.of("UTC"))
        aiMasterData.setUpdateTime(LocalDateTime.now());
        aiMasterData.setAiBody(map.get("aiData"));
        aiMasterData.setUserBody(data);
        aiMasterData.setAiMessageSessionId(aiMessageSession.getId());

        // 处理文件（头像等）
        if (file != null && !file.isEmpty()) {
            try {
                byte[] avatarBytes = file.getBytes();
                aiMasterData.setPic(avatarBytes);  // 将字节数组直接设置到 pic 字段
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件读取错误");
            }
        }

        // 插入数据库并返回
        int insertResult = masterdataMapper.insert(aiMasterData);
        ThrowUtils.throwIf(insertResult <= 0, ErrorCode.DATABASE_INSERT_ERROR);

        return aiMasterData;
    }



    @Override
    public void ensureAndUpdateAiMasterData(Map<String, String> map, String data) {
        long userId = Long.parseLong(map.get("userId"));

        // 确保会话存在
        Boolean isAiMessageSessionExists = ensureAiMessageSessionExists(userId, data);
        ThrowUtils.throwIf(!isAiMessageSessionExists, ErrorCode.ADD_DATABASE_ERROR, "User " + userId + " does not have an active session");

        // 更新 AI Master Data
        updatedAiMasterData(map, data, null);
    }

    @Override
    public void ensureAndCreateAiMasterDataForCourse(Map<String, String> map, String data) {
        long userId = Long.parseLong(map.get("userId"));

        // 确保会话存在
        Boolean isAiMessageSessionExists = ensureAiMessageSessionExists(userId, data);
        ThrowUtils.throwIf(!isAiMessageSessionExists, ErrorCode.ADD_DATABASE_ERROR, "User " + userId + " does not have an active session");

        // 创建并保存 AI Master Data
        createAndSaveAiMasterData(map, data, null);
    }

    @Override
    public AIMasterData createAndSaveAiMasterDataWithFile(Map<String, String> map, MultipartFile file, String title) {
        // 调用创建和保存逻辑，返回 AIMasterData 对象
        return createAndSaveAiMasterData(map, title, file);
    }



    /**
     * @Author: Mr.kimo
     * @Date: 18:24
     * @return:
     * @Param:
     * @Description: 获取所有图标，已更新版本，已弃用
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {


        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        int current = chartQueryRequest.getCurrent();
        int pageSize = chartQueryRequest.getPageSize();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), SqlConstants.CHART_ID, id);
        queryWrapper.like(StringUtils.isNotBlank(name), SqlConstants.CHART_NAME, name);
        queryWrapper.like(StringUtils.isNotBlank(goal), SqlConstants.CHART_GOAL, goal);
        queryWrapper.like(StringUtils.isNotBlank(chartType), SqlConstants.CHART_TYPE, chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), SqlConstants.USER_ID, userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}




