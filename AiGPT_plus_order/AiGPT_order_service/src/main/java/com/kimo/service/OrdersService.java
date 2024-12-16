package com.kimo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.messagesdk.model.po.MqMessage;
import com.kimo.model.dto.AddOrderDto;
import com.kimo.model.dto.PayRecordDto;
import com.kimo.model.dto.PayRecordRequestDto;
import com.kimo.model.dto.PayStatusDto;
import com.kimo.model.po.Orders;
import com.kimo.model.po.OrdersRecord;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Mr.kimo
 */
public interface OrdersService extends IService<Orders> {

    PayRecordDto createOrder(AddOrderDto addOrderDto, HttpServletRequest request);

    /**
     * 查询支付记录
     * @param payNo
     * @return
     */
    public OrdersRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    public PayRecordDto queryPayResult(String payNo,HttpServletRequest request);

    public void saveAliPayStatus(PayStatusDto payStatusDto,Long userId);

    /**
     * 发送通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);

    PayRecordDto getPayCodeForOrderId(PayRecordRequestDto recordDto, HttpServletRequest request);

    Orders saveOrders(String string, AddOrderDto addOrderDto);
}
