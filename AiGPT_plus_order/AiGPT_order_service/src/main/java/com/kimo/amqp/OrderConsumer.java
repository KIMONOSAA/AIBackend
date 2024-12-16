package com.kimo.amqp;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kimo.common.ErrorCode;
import com.kimo.exception.ThrowUtils;
import com.kimo.mapper.OrdersDetailMapper;
import com.kimo.mapper.OrdersMapper;
import com.kimo.mapper.OrdersRecordMapper;
import com.kimo.messagesdk.model.po.MqMessage;
import com.kimo.model.po.Orders;
import com.kimo.model.po.OrdersDetail;
import com.kimo.model.po.OrdersRecord;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.kimo.constant.RabbitMQConstant.ORDER_QUEUE;
import static com.kimo.constant.RabbitMQConstant.PAYNOTIFY_QUEUE;


@Component
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {


    private final OrdersMapper ordersMapper;
    private final OrdersDetailMapper ordersDetailMapper;
    private final OrdersRecordMapper ordersRecordMapper;



    @RabbitHandler
    @RabbitListener(queues = ORDER_QUEUE)
    public void handleOrderCancel(Message orderMessage, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        byte[] body = orderMessage.getBody();
        String jsonString = new String(body, StandardCharsets.UTF_8);
        Orders mqMessage;

        try {
            mqMessage = JSON.parseObject(jsonString, Orders.class);
        } catch (Exception e) {
            log.error("订单消息解析失败，消息内容：{}", jsonString, e);
            channel.basicNack(deliveryTag, false, false); // 丢弃消息
            return;
        }

        try {
            cancelOrder(mqMessage);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("订单取消失败，消息内容：{}", jsonString, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @Transactional
    protected void cancelOrder(Orders orderMessage) {
        Long orderId = orderMessage.getId();
        Orders orders = ordersMapper.selectById(orderId);
        ThrowUtils.throwIf(orders == null, ErrorCode.NOT_FOUND_ERROR, "查无订单");

        // 删除主表
        ordersMapper.deleteById(orders);

        // 删除详细表和记录表
        ordersDetailMapper.delete(new QueryWrapper<OrdersDetail>().eq("order_id", orderId));
        ordersRecordMapper.delete(new QueryWrapper<OrdersRecord>().eq("order_id", orderId));
    }
}