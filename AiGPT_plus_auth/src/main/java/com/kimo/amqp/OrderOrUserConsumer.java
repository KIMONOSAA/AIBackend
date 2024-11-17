package com.kimo.amqp;

import com.alibaba.fastjson2.JSON;
import com.kimo.common.PageRequest;


import com.kimo.constant.RedisConstant;
import com.kimo.messagesdk.model.po.MqMessage;
import com.kimo.ucenter.service.UserService;
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
import org.springframework.amqp.core.Message;
import java.io.IOException;


import static com.kimo.constant.RabbitMQConstant.PAYNOTIFY_QUEUE;


/**
 * @Author kimo
 * @Description 监听订单成功后生产者发送消息
 * @Date
 **/
@Component
@Slf4j
public class OrderOrUserConsumer {


    @Autowired
    private UserService userService;


    @Autowired
    private RedissonClient redissonClient;



    @RabbitHandler
    @RabbitListener(queues = PAYNOTIFY_QUEUE)
    public void processChart(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        byte[] body = message.getBody();
        String JsonString = new String(body);
        MqMessage mqMessage = JSON.parseObject(JsonString, MqMessage.class);
        //会员类型ID
        String MemberId = mqMessage.getBusinessKey1();
        //类型
        String MemberType = mqMessage.getBusinessKey2();
        //用户id
        String userId = mqMessage.getBusinessKey3();
        RLock lock = redissonClient.getLock(RedisConstant.ORDERS_USER_KEY + userId);
        lock.lock();
        try {
            //根据消息内容，跟新个人记录和积分
            boolean b = userService.saveUserStatusSuccess(MemberId, MemberType, userId);
            channel.basicAck(deliveryTag,false);
        }catch (Exception e){
            log.error(e.getMessage());
            channel.basicNack(deliveryTag,false,true);
        }finally {
            lock.unlock();
        }
    }



}
