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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

import static com.kimo.constans.CouZiConstant.PRACTICE_CHART_BOT_ID;
import static com.kimo.constans.CouZiConstant.PRACTICE_CHART_BOT_TOKEN;
import static com.kimo.constant.RabbitMQConstant.PRACTICE_DIRECT_QUEUE;

@Component
@Slf4j
/**
 * @Author: Mr.kimo
 * @Date: 10:57
 * @return:
 * @Param:
 * @Description: 根据学生练习记录AI生成评估
 */
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
        if (StringUtils.isEmpty(message)) {
            log.error("收到空消息");
            channel.basicNack(deliveryTag, false, true);
            return;
        }

        try {
            // 反序列化并进行空值检查
            Map<String, String> receivedMessage = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
            String chartDataRequestJson = receivedMessage.get("chartDataRequest");
            String practiceRecordJson = receivedMessage.get("practiceRecord");

            if (StringUtils.isEmpty(chartDataRequestJson) || StringUtils.isEmpty(practiceRecordJson)) {
                log.error("收到不完整的数据：chartDataRequest 或 practiceRecord 缺失");
                channel.basicNack(deliveryTag, false, true);
                return;
            }

            ChartDataRequest oldChartDataRequest = objectMapper.readValue(chartDataRequestJson, ChartDataRequest.class);
            PracticeRecordDto oldPracticeRecord = objectMapper.readValue(practiceRecordJson, PracticeRecordDto.class);
            String userId = oldPracticeRecord.getUserId().toString();

            // 业务校验
            ThrowUtils.throwIf(oldChartDataRequest == null, ErrorCode.NOT_LOGIN_ERROR);
            ThrowUtils.throwIf(oldPracticeRecord == null, ErrorCode.NOT_LOGIN_ERROR);

            long recordId = oldPracticeRecord.getId();
            PracticeRecordPro practiceRecordPro = new PracticeRecordPro();
            BeanUtils.copyProperties(oldPracticeRecord, practiceRecordPro);

            ChartDataRequest chartDataRequest = new ChartDataRequest();
            BeanUtils.copyProperties(oldChartDataRequest, chartDataRequest);

            GouZiAdditionalMessages gouZiAdditionalMessages = new GouZiAdditionalMessages();
            gouZiAdditionalMessages.setRole("user");
            gouZiAdditionalMessages.setContent(chartDataRequest.getChartData());
            gouZiAdditionalMessages.setContent_type("text");

            String botId = PRACTICE_CHART_BOT_ID;
            String user = oldPracticeRecord.getUserId().toString();
            String token = PRACTICE_CHART_BOT_TOKEN;

            // 使用锁仅锁定必要部分
            RLock lock = redissonClient.getLock(RedisConstant.USER_INFO_ID_PRE + recordId);
            lock.lock();
            try {
                String chartData = chartService.getChartDataForCouZiChartAndFileData(gouZiAdditionalMessages, null, botId, user, userId, token);
                practiceRecordPro.setAiresult(chartData);
                practiceRecordProMapper.insert(practiceRecordPro);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("处理 recordId： {} 的图表数据时出错。异常：{}", recordId, e.getMessage());
                channel.basicNack(deliveryTag, false, true);
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            log.error("反序列化消息 {} 时出错", e.getMessage());
            channel.basicNack(deliveryTag, false, true);
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
