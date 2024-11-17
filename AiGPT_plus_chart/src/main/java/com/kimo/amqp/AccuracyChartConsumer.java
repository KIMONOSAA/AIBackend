package com.kimo.amqp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.constant.RedisConstant;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.mapper.AccuracyChartMapper;
import com.kimo.model.dto.chart.Accuracy;
import com.kimo.model.dto.po.AccuracyChart;
import com.kimo.model.dto.po.Chart;
import com.kimo.service.AccuracyChartService;
import com.kimo.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.kimo.constans.CouZiConstant.ACCURACY_CHART_BOT_ID;
import static com.kimo.constans.CouZiConstant.ACCURACY_CHART_BOT_TOKEN;
import static com.kimo.constant.RabbitMQConstant.ACCURACY_DIRECT_QUEUE;

@Component
@Slf4j
/**
 * @Author: Mr.kimo
 * @Date: 11:20
 * @return:
 * @Param:
 * @Description: 消费者异步AI生成可视化视频图表
 */
public class AccuracyChartConsumer {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AccuracyChartMapper accuracyChartMapper;

    @Autowired
    private AccuracyChartService accuracyChartService;

    @Autowired
    private ChartService chartService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RabbitHandler
    @RabbitListener(queues = ACCURACY_DIRECT_QUEUE, ackMode = "MANUAL")
    /**
     * @Author: Mr.kimo
     * @Date: 11:08
     * @return: void
     * @Param: [java.lang.String, com.rabbitmq.client.Channel, long]
     * @Description: 根据准确率AI生成可视化图表
     */
    public void processChart(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            //方序列化对象
            Accuracy accuracy = objectMapper.readValue(message, Accuracy.class);
            long accuracyId = accuracy.getUserId();

            // 锁定当前用户，避免并发问题
            RLock lock = redissonClient.getLock(RedisConstant.USER_INFO_ID_PRE + accuracyId);
            lock.lock();

            try {
                AccuracyChart accuracyChart = getAccuracyChart(accuracyId);
                if (accuracyChart == null) {
                    accuracyChart = createNewAccuracyChart(accuracyId);
                }

                if ("succeed".equals(accuracyChart.getStatus())) {
                    log.info("消息体参数 【重复消息】: {}", accuracyChart.getId());
                    return;
                }

                accuracyChart.setStatus("running");
                accuracyChart.setUpdateTime(LocalDateTime.now());

                // 调用外部服务生成图表数据
                String chartData = generateChartDataForUser(accuracy, accuracyId);
                if (chartData == null) {
                    handleChartUpdateError(accuracyChart.getId(), "图表生成失败");
                    return;
                }

                String[] splits = chartData.split("【【【【【");
                if (splits.length < 3) {
                    handleChartUpdateError(accuracyChart.getId(), "图表生成格式错误");
                    return;
                }

                String genChart = splits[1].trim();
                String genResult = splits[2].trim();

                // 更新图表信息
                accuracyChart.setGenChat(genChart);
                accuracyChart.setGenResult(genResult);
                accuracyChart.setStatus("succeed");

                saveOrUpdateAccuracyChart(accuracyChart);

                // 确认消息
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("处理消息时发生错误，错误信息：", e);
                channel.basicNack(deliveryTag, false, true);
            } finally {
                lock.unlock();
            }

        } catch (IOException e) {
            log.error("反序列化消息失败，错误信息：", e);
        }
    }

    private AccuracyChart getAccuracyChart(long accuracyId) {
        QueryWrapper<AccuracyChart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", accuracyId);
        return accuracyChartMapper.selectOne(queryWrapper);
    }

    private AccuracyChart createNewAccuracyChart(long accuracyId) {
        AccuracyChart accuracyChart = new AccuracyChart();
        accuracyChart.setUserId(accuracyId);
        accuracyChart.setCreateTime(LocalDateTime.now());
        return accuracyChart;
    }

    private String generateChartDataForUser(Accuracy accuracy, long accuracyId) {
        GouZiAdditionalMessages gouZiAdditionalMessages = new GouZiAdditionalMessages();
        gouZiAdditionalMessages.setContent(accuracy.getAccuracy());
        gouZiAdditionalMessages.setRole("user");
        gouZiAdditionalMessages.setContent_type("text");

        String botId = ACCURACY_CHART_BOT_ID;
        String user = accuracy.getUserId().toString();
        String token = ACCURACY_CHART_BOT_TOKEN;

        try {
            return chartService.getChartDataForCouZiChartAndFileData(gouZiAdditionalMessages, null, botId, user, String.valueOf(accuracyId), token);
        } catch (Exception e) {
            handleChartUpdateError(accuracyId, "AI生成错误");
        }
        return null;
    }

    private void saveOrUpdateAccuracyChart(AccuracyChart accuracyChart) {
        if (accuracyChart.getId() == null) {
            accuracyChartMapper.insert(accuracyChart);
        } else {
            accuracyChartMapper.updateById(accuracyChart);
        }
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("图表状态更改失败");
        }
    }
}