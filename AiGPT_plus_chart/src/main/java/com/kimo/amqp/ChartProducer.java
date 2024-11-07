package com.kimo.amqp;


import com.kimo.config.ApplicationContextFactory;
import com.kimo.constant.RabbitMQConstant;
import com.kimo.model.dto.po.Chart;
import com.kimo.model.dto.enums.RabbitMQEnum;
import com.kimo.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;
/**
 * @author Mr.kimo
 */
@Component
@Slf4j
public class ChartProducer {

    @Lazy
    @Autowired
    private ChartService chartService;

    public void sendMessage(String message){
        RabbitTemplate rabbitTemplate = ApplicationContextFactory.getBean(RabbitTemplate.class);
        CorrelationData correlation = new CorrelationData(UUID.randomUUID().toString());
        long chartId = Long.parseLong(message);
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            if(ack){
                log.info("发送成功到交换机");
            }else{
                Chart updateChartResult = new Chart();
                updateChartResult.setId(chartId);
                updateChartResult.setStatus(RabbitMQEnum.FAILED.toString());
                updateChartResult.setExecMessage("发送消息到交换机时发送错误");
                boolean updateResult = chartService.updateById(updateChartResult);
                if(!updateResult){
                    log.error("图表状态更改失败");
                }
            }
        });

        rabbitTemplate.convertAndSend(RabbitMQConstant.DIRECT_EXCHANGE,RabbitMQConstant.DIRECT_ROUTING_KEY,chartId,chartId1 -> {
            chartId1.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return chartId1;
        },correlation);
    }
}
