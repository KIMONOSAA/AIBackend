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

import static com.kimo.constant.RabbitMQConstant.ACCURACY_DIRECT_QUEUE;

/**
 * @author Mr.kimo
 */
@Component
@Slf4j
public class AccuracyChartConsumer {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AccuracyChartMapper accuracyChartMapper;

    @Autowired
    private AccuracyChartService accuracyChartService;



    @Autowired
    private ChartService chartService;

    private ObjectMapper objectMapper = new ObjectMapper(); // 用于反序列化

    @RabbitHandler
    @RabbitListener(queues = ACCURACY_DIRECT_QUEUE, ackMode = "MANUAL")
    public void processChart(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        // 反序列化 Accuracy 对象
        Accuracy accuracy = objectMapper.readValue(message, Accuracy.class);
        long accuracyId = accuracy.getUserId(); // 假设 Accuracy 有一个 getId() 方法
        String s = String.valueOf(accuracyId);
        RLock lock = redissonClient.getLock(RedisConstant.USER_INFO_ID_PRE + accuracyId);
        lock.lock();
        try {
            QueryWrapper<AccuracyChart> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", accuracyId);
            Boolean st = false;
            AccuracyChart accuracyChart = accuracyChartMapper.selectOne(queryWrapper);
            if (accuracyChart == null) {
                st = true;
                accuracyChart = new AccuracyChart();
                accuracyChart.setCreateTime(LocalDateTime.now());
            }
            if ("succeed".equals(accuracyChart.getStatus())) {
                log.info("消息体参数 【重复消息】: {}", accuracyChart.getId());
                return;
            }

            accuracyChart.setStatus("running");

            accuracyChart.setUpdateTime(LocalDateTime.now());
            GouZiAdditionalMessages gouZiAdditionalMessages = new GouZiAdditionalMessages();
            gouZiAdditionalMessages.setContent(accuracy.getAccuracy());
            gouZiAdditionalMessages.setRole("user");
            gouZiAdditionalMessages.setContent_type("text");
            String botId = "7432966743104520192";
            String user = "user";
            String token = "pat_7gwklsLnL5KGDMGecF6IuLazLWBNDqwyELV7nGUGrD215fi1D2yjWSKkzSSiVijO";
            String chartDataForCouZi = chartService.getChartDataForCouZiChartAndFileData(gouZiAdditionalMessages,null,botId,user,s,token);
            String[] splits = chartDataForCouZi.split("【【【【【");
            if (splits.length < 3) {
                handleChartUpdateError(accuracyChart.getId(), "图表生成格式错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            accuracyChart.setGenChat(genChart);
            accuracyChart.setGenResult(genResult);
            accuracyChart.setStatus("succeed");
            accuracyChart.setUserId(accuracy.getUserId());

            if (st){
                accuracyChartMapper.insert(accuracyChart);
            }else {
                accuracyChartMapper.updateById(accuracyChart);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error(e.getMessage());
            channel.basicNack(deliveryTag, false, true);
        } finally {
            lock.unlock();
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
