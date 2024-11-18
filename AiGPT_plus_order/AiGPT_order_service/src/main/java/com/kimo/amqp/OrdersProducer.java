package com.kimo.amqp;


import com.alibaba.fastjson2.JSON;

import com.kimo.config.ApplicationContextFactory;
import com.kimo.constant.RabbitMQConstant;
import com.kimo.messagesdk.model.po.MqMessage;
import com.kimo.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
/**
 * @author Mr.kimo
 */
@Component
@Slf4j
public class OrdersProducer {


    @Autowired
    private MqMessageService mqMessageService;

    public void sendMessage(MqMessage message){
        //消息内容
        String jsonString = JSON.toJSONString(message);
        //创建一个持久化消息
        Message messageObj = MessageBuilder.withBody(jsonString.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

        RabbitTemplate rabbitTemplate = ApplicationContextFactory.getBean(RabbitTemplate.class);

        Long id = message.getId();
        CorrelationData correlation = new CorrelationData(id.toString());
//        long chartId = Long.parseLong(message);
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            if(ack){
                log.info("发送成功到交换机");
                mqMessageService.completed(id);
            }else{
                // 投递失败
                MqMessage mqMessage = JSON.parseObject(message.toString(), MqMessage.class);
                //将消息再添加到消息表
                mqMessageService.addMessage(mqMessage.getMessageType(),mqMessage.getBusinessKey1(),mqMessage.getBusinessKey2(),mqMessage.getBusinessKey3());
            }
        });
//        rabbitTemplate.setReturnsCallback((returnedMessage, replyCode, replyText, exchange, routingKey) -> {
//            // 获取消息体内容
//            String messageBody = new String(returnedMessage.getMessage().getBody(), StandardCharsets.UTF_8);
//
//            // 记录投递失败日志
//            log.info("消息发送失败，应答码：{}，原因：{}，交换机：{}，路由键：{}，消息内容：{}",
//                    replyCode, replyText, exchange, routingKey, messageBody);
//
//            // 将消息重新添加到消息表
//            MqMessage mqMessage = JSON.parseObject(messageBody, MqMessage.class);
//            mqMessageService.addMessage(
//                    mqMessage.getMessageType(),
//                    mqMessage.getBusinessKey1(),
//                    mqMessage.getBusinessKey2(),
//                    mqMessage.getBusinessKey3());
//        });

        rabbitTemplate.convertAndSend(RabbitMQConstant.PAYNOTIFY_EXCHANGE_FANOUT,"",messageObj,correlation);
    }
}
