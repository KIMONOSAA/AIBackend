package com.kimo.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.config.ApplicationContextFactory;
import com.kimo.constant.RabbitMQConstant;
import com.kimo.exception.BusinessException;
import com.kimo.model.dto.ChartDataForRecord;
import com.kimo.model.po.Accuracy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PracticeChartProducer {

    public void sendMessage(ChartDataForRecord message){
        RabbitTemplate rabbitTemplate = ApplicationContextFactory.getBean(RabbitTemplate.class);
        CorrelationData correlation = new CorrelationData(UUID.randomUUID().toString());
        ObjectMapper objectMapper = new ObjectMapper();
        // 将 Accuracy 对象序列化为 JSON 字符串
        try {
            // 将多个对象封装到 Map 中
            Map<String, String> combinedMessage = new HashMap<>();
            combinedMessage.put("chartDataRequest", objectMapper.writeValueAsString(message.getChartDataRequest()));
            combinedMessage.put("practiceRecord", objectMapper.writeValueAsString(message.getPracticeRecord()));

            // 将 Map 转换为 JSON 字符串
            String combinedMessageJson = objectMapper.writeValueAsString(combinedMessage);
//            rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
//                if(ack){
//                    log.info("发送成功到交换机");
//                }else{
//                    log.error("图表状态更改失败");
//
//                }
//            });
            rabbitTemplate.convertAndSend(RabbitMQConstant.PRACTICE_DIRECT_EXCHANGE,
                    RabbitMQConstant.PRACTICE_DIRECT_ROUTING_KEY,
                    combinedMessageJson,
                    message1 -> {
                        message1.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message1;
                    },
                    correlation);
        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

    }
}
