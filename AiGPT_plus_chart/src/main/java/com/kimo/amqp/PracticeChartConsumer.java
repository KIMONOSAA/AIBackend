package com.kimo.amqp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.constant.RedisConstant;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.exception.ThrowUtils;

import com.kimo.mapper.PracticeRecordProMapper;

import com.kimo.model.dto.chart.ChartDataForRecord;
import com.kimo.model.dto.chart.ChartDataRequest;
import com.kimo.model.dto.chart.PracticeRecord;

import com.kimo.model.dto.chart.PracticeRecordDto;
import com.kimo.model.dto.po.Chart;
import com.kimo.model.dto.po.PracticeRecordPro;

import com.kimo.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.kimo.constant.RabbitMQConstant.PRACTICE_DIRECT_QUEUE;
/**
 * @author Mr.kimo
 */
@Component
@Slf4j
public class PracticeChartConsumer {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private PracticeRecordProMapper practiceRecordProMapper;

    @Autowired
    private ChartService chartService;

    private ObjectMapper objectMapper = new ObjectMapper(); // 用于反序列化

    @RabbitHandler
    @RabbitListener(queues = PRACTICE_DIRECT_QUEUE, ackMode = "MANUAL")
    public void processChart(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        // 反序列化 Accuracy 对象
        Map<String, String> receivedMessage = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});

        // 提取单独的 JSON 数据
        String chartDataRequestJson = receivedMessage.get("chartDataRequest");
        String practiceRecordJson = receivedMessage.get("practiceRecord");

        ChartDataRequest oldChartDataRequest = objectMapper.readValue(chartDataRequestJson, ChartDataRequest.class);
        PracticeRecordDto oldPracticeRecord = objectMapper.readValue(practiceRecordJson, PracticeRecordDto.class);
        ThrowUtils.throwIf(oldChartDataRequest == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(oldPracticeRecord == null,ErrorCode.NOT_LOGIN_ERROR);
        long recordId = oldPracticeRecord.getId(); // 假设 Accuracy 有一个 getId() 方法
        PracticeRecord newPracticeRecord = new PracticeRecord();
        ChartDataRequest chartDataRequest = new ChartDataRequest();
        PracticeRecordPro practiceRecordPro = new PracticeRecordPro();
        BeanUtils.copyProperties(oldPracticeRecord, practiceRecordPro);
        BeanUtils.copyProperties(oldChartDataRequest, chartDataRequest);
        GouZiAdditionalMessages gouZiAdditionalMessages = new GouZiAdditionalMessages();
        gouZiAdditionalMessages.setRole("user");
        gouZiAdditionalMessages.setContent(chartDataRequest.getChartData());
        gouZiAdditionalMessages.setContent_type("text");
        String botId = "7433801236367556627";
        String user = "user";
        String token = "pat_NLRWUk2TjsJT8Q7EwIIafUfOIBzUPuMzeaQJRWcnW16wHc3hbAKbRggidi0tH1Xm";
        RLock lock = redissonClient.getLock(RedisConstant.USER_INFO_ID_PRE + recordId);
        lock.lock();
        try {
            String chartData = chartService.getChartDataForCouZiChart(gouZiAdditionalMessages,botId,user,token);
            practiceRecordPro.setAiresult(chartData);
            practiceRecordProMapper.insert(practiceRecordPro);
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
