package com.kimo.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.amqp.ChartProducer;
import com.kimo.common.ErrorCode;
import com.kimo.constant.ChartConstant;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.domain.CouZiCompletionEventResponse;
import com.kimo.domain.CouZiCompletionRequest;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.manager.RedisLimiterManager;
import com.kimo.mapper.ChartMapper;

import com.kimo.model.dto.chart.*;
import com.kimo.model.dto.po.Chart;

import com.kimo.model.dto.vo.BiResponse;
import com.kimo.service.ChartService;
import com.kimo.session.CoZeConfiguration;
import com.kimo.session.CoZeSession;
import com.kimo.session.defaults.DefaultCoZeSessionFactory;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.kimo.constants.CouZiConstant.BEARER;
import static com.kimo.utils.ExcelUtils.excelToCsv;

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

//    @Resource
//    private PointMapper pointMapper;

    @Autowired
    private CoZeSession coZeSession;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Autowired
    private ServletUtils  servletUtils;

    @Override
    public Long getLoginUser(HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto loginUser = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(loginUser.getId() <= 0,ErrorCode.OPERATION_ERROR);
        return loginUser.getId();
    }

//    @Override
//    public String getChartData(ChartDataRequest chartData) throws Exception {
//        CompletionRequest aiMasterDataRequest = CompletionRequest.builder()
//                .stream(true)
//                .messages(Collections.singletonList(Text.builder().role(CompletionRequest.Role.USER.getCode()).content(chartData.getChartData()).build()))
//                .model(CompletionRequest.Model.XUNFEI.getCode())
//                .build();
//
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//
//        XunFeiEventSourceListener xunFeiEventSourceListener = new XunFeiEventSourceListener(countDownLatch);
//
//        EventSource eventSource = executor.completions(aiMasterDataRequest,null, xunFeiEventSourceListener);
//
//        countDownLatch.await();
//        log.info(eventSource.toString());
//
//        return xunFeiEventSourceListener.getAnswer();
//    }

    @Override
    public String getChartDataForCouZiChart(GouZiAdditionalMessages chartData,String botId,String user,String token) throws Exception {
        List<String> answerContents = new ArrayList<>();
        String lastAnswerContent = null;
        // 1. 创建参数
        ArrayList<GouZiAdditionalMessages> goZeAdditionalMessages1 = new ArrayList<>();
//        GouZiAdditionalMessages goZeAdditionalMessages = new GouZiAdditionalMessages();
//        goZeAdditionalMessages.setContent("分析需求:分析网站用户成绩信息，原始数据:[{科目,成绩语文,60数学,20英语,30，使用折线图},{科目,成绩语文,40数学,10英语,70，使用折线图},{科目,成绩语文,35数学,60英语,77，使用折线图}]");
//        goZeAdditionalMessages.setRole("user");
//        goZeAdditionalMessages.setContent_type("text");
        goZeAdditionalMessages1.add(chartData);
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
        CoZeConfiguration yuanQiConfiguration = new CoZeConfiguration();
        yuanQiConfiguration.setApiHost("https://api.coze.cn/");
        yuanQiConfiguration.setApiKey(BEARER + token);
        yuanQiConfiguration.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        // 2. 会话工厂
        DefaultCoZeSessionFactory factory = new DefaultCoZeSessionFactory(yuanQiConfiguration);
        // 3. 开启会话
        this.coZeSession = factory.openSession();
        // 2. 请求等待
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventSource eventSource = coZeSession.chatCompletions(null,null,chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
                try {
                    CouZiCompletionEventResponse response = JSONUtil.toBean(data, CouZiCompletionEventResponse.class);


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
           lastAnswerContent = answerContents.get(answerContents.size() - 1);
        }
        return lastAnswerContent;
    }




//    @Override
//    public String getChartData(String chartData) throws Exception {
//        ChartDataRequest chartDataRequest = new ChartDataRequest();
//        chartDataRequest.setChartData(chartData);
//        return this.getChartData(chartDataRequest);
//    }

    @Override
    public Boolean deletedChart(HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto user = userClient.GobalGetLoginUser(username);
        long id = user.getId();
        // 判断是否存在
        Chart oldChart = chartMapper.selectById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userClient.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        int id1 = chartMapper.deleteById(id);
        return id1 > 0;
    }

    @Override
    public Page<Chart> listMyChartByPage(HttpServletRequest request, ChartQueryRequest chartQueryRequest) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto user = userClient.GobalGetLoginUser(username);

        chartQueryRequest.setUserId(user.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
    }

    @Override
    public Boolean editChart(HttpServletRequest request, ChartEditRequest chartEditRequest) {
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto user = userClient.GobalGetLoginUser(username);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartMapper.selectById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(user.getId()) && !userClient.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        int updateById = chartMapper.updateById(chart);
        return updateById > 0;
    }

    @Override
    public Page<Chart> listChartUserByPage(long current, long size, ChartQueryRequest chartQueryRequest) {
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
    }

    @Override
    public BiResponse genChartByAIRabbitMQ(MultipartFile multipartFile, GenChartyByAIRequest genChartByAI, HttpServletRequest request) {
        String name = genChartByAI.getName();
        String goal = genChartByAI.getGoal();
        String chartType = genChartByAI.getChartType();

        //校验
//        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
//        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100,ErrorCode.PARAMS_ERROR,"名称过长");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024L * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList(ChartConstant.SPECIFICATION_XLSX, ChartConstant.SPECIFICATION_XLS);
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
//        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
//        UserDto loginUser = userClient.GobalGetLoginUser(username);
//        redisLimiterManager.doRateLimit(ChartConstant.GEN_CHAR_KEY + loginUser.getId());

        StringBuilder userInput = new StringBuilder();
        userInput.append(ChartConstant.ANALYZE_DEMAND).append("\n");
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal += ChartConstant.UTILIZATION + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append(ChartConstant.RAW_DATA).append("\n");

        String resultData = excelToCsv(multipartFile);
        userInput.append(resultData).append("\n");
//        this.increase(loginUser);


        Chart chart = new Chart();

        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(resultData);
        chart.setChartType(chartType);
        chart.setStatus(ChartConstant.IS_wait);

//        chart.setUserId(loginUser.getId());
        int insert = chartMapper.insert(chart);
        ThrowUtils.throwIf(insert <= 0,ErrorCode.SYSTEM_ERROR,"图表保存失败");
        chartProducer.sendMessage(String.valueOf(chart.getId()));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

//    private synchronized void increase(UserDto user){
//        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("userId",user.getId());
//        Point point = pointMapper.selectOne(queryWrapper);
//        ThrowUtils.throwIf(point == null,ErrorCode.OPERATION_ERROR);
//        ThrowUtils.throwIf(point.getPoint() < 5,ErrorCode.OPERATION_ERROR);
//        point.setPoint(point.getPoint() - 10);
//        pointMapper.updateById(point);
//    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
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




