package com.kimo.service.impl;

import com.alibaba.fastjson2.JSON;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.shaded.com.google.gson.JsonSyntaxException;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.amqp.OrderProducer;
import com.kimo.amqp.OrdersProducer;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.config.AlipayConfig;
import com.kimo.constant.SecurityConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;

import com.kimo.mapper.OrdersDetailMapper;
import com.kimo.mapper.OrdersMapper;
import com.kimo.mapper.OrdersRecordMapper;
import com.kimo.messagesdk.model.po.MqMessage;
import com.kimo.messagesdk.service.MqMessageService;
import com.kimo.model.dto.AddOrderDto;
import com.kimo.model.dto.PayRecordDto;
import com.kimo.model.dto.PayRecordRequestDto;
import com.kimo.model.dto.PayStatusDto;

import com.kimo.model.po.Orders;
import com.kimo.model.po.OrdersDetail;
import com.kimo.model.po.OrdersRecord;
import com.kimo.service.OrdersService;
import com.kimo.utils.IdWorkerUtils;
import com.kimo.utils.QRCodeUtil;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Mr.kimo
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService {


    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private OrdersDetailMapper ordersDetailMapper;

    @Autowired
    private OrdersRecordMapper ordersRecordMapper;

    @Autowired
    @Lazy
    OrdersServiceImpl currentProxy;

    @Autowired
    private OrdersProducer ordersProducer;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private OrderProducer  orderProducer;

    @Autowired
    @Lazy
    private OrdersService currentProxyService;


    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @Value("${pay.qrcodeurl}")
    String qrcodeUrl;

    @Override
    @Transactional
    public PayRecordDto createOrder(AddOrderDto addOrderDto, HttpServletRequest request) {

        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        Long id = userDto.getId();
        //插入订单表，详细表
        Orders orders = currentProxyService.saveOrders(id.toString(), addOrderDto);
        

        //添加支付记录表
        OrdersRecord payRecord = createPayRecord(orders);

        Long payNo = payRecord.getPayNo();

        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
//        System.out.println(qrCodeUtil.createQRCode("http://192.168.101.1:65010/order/requestpay?payNo=" + payNo, 200, 200));
        String qrCode = null;
        try {
            //url要可以被模拟器访问到，url为下单接口(稍后定义)
            String url = String.format(qrcodeUrl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GENERATING_THE_QR_CODE_ERROR);
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);

        int delayTime = 30 * 60 * 1000;
//        int delayTime = 30 * 1000;
        orderProducer.sendMessage(orders,delayTime);
        return payRecordDto;
    }

    @Override
    public OrdersRecord getPayRecordByPayno(String payNo) {
        return ordersRecordMapper.selectOne(new LambdaQueryWrapper<OrdersRecord>().eq(OrdersRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo, HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDto.getId();
        ThrowUtils.throwIf(userId <= 0,ErrorCode.NOT_LOGIN_ERROR);
        //调用支付宝的接口查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //拿到支付结果更新支付记录表订单表的支付状态
        currentProxy.saveAliPayStatus(payStatusDto,userId);
        //返回最新的支付记录信息
        OrdersRecord payRecordByPayno = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordByPayno,payRecordDto);
        return payRecordDto;
    }

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo){
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        //bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body = null;
        try {
            response = alipayClient.execute(request);
            if(!response.isSuccess()){
                throw new BusinessException(ErrorCode.TRADING_NOT_FOUND);
            }
            body = response.getBody();
        } catch (AlipayApiException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        PayStatusDto payStatusDto = new PayStatusDto();


        Map map = JSON.parseObject(body, Map.class);
        Map alipay_trade_query_response = (Map) map.get("alipay_trade_query_response");
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_no((String) alipay_trade_query_response.get("trade_no"));
        payStatusDto.setTrade_status((String) alipay_trade_query_response.get("trade_status"));
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount((String) alipay_trade_query_response.get("total_amount"));

        return payStatusDto;
    }

    /**
     * @description 保存支付宝支付结果
     * @param payStatusDto  支付结果信息
     */
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto,Long userId){


        //支付记录号
        String outTradeNo = payStatusDto.getOut_trade_no();
        OrdersRecord payRecordByPayno = getPayRecordByPayno(outTradeNo);
        Long userId1 = payRecordByPayno.getUserId();
        ThrowUtils.throwIf(payRecordByPayno == null,ErrorCode.PAYRECORD_NOT_FOUND);
        //拿到相关联的订单id
        Long orderId = payRecordByPayno.getOrderId();
        Orders orders = ordersMapper.selectById(orderId);
        ThrowUtils.throwIf(orders == null,ErrorCode.ORDER_NOT_FOUNT);
        //支付状态
        String statusFromDb = payRecordByPayno.getStatus();
        //判断数据库支付的状态是否已经成功了，就不再处理了
        if("601002".equals(statusFromDb)){
            //如果已经成功
            return ;
        }

        //如果支付成功
        String tradeStatus = payStatusDto.getTrade_status();//从支付宝查询的支付结果
        if("TRADE_SUCCESS".equals(tradeStatus)){//支付表返回的信息是否为支付成功
            //更新支付记录表的状态为支付成功
            payRecordByPayno.setStatus("601002");
            //支付表的订单号
            payRecordByPayno.setOutPayNo(payStatusDto.getTrade_no());
            //第三方支付渠道号
            payRecordByPayno.setOutPayChannel("Alipay");
            //支付成功时间
            payRecordByPayno.setPaySuccessTime(LocalDateTime.now());
            ordersRecordMapper.updateById(payRecordByPayno);

            //更新订单表的支付状态为支付成功
            orders.setStatus("600002");
            ordersMapper.updateById(orders);

            //将消息写到数据库
            MqMessage payresultNotify = mqMessageService.addMessage("payresult_notify", orders.getOutBusinessId(), orders.getOrderType(), userId1.toString());

            //发送消息
            notifyPayResult(payresultNotify);
        }

    }

    @Override
    public void notifyPayResult(MqMessage message) {
        ordersProducer.sendMessage(message);
    }

    @Override
    public PayRecordDto getPayCodeForOrderId(PayRecordRequestDto recordDto, HttpServletRequest request) {
        ThrowUtils.throwIf(recordDto == null,ErrorCode.PAYRECORD_NOT_FOUND);
        Long payRecordId = recordDto.getPayRecordId();
        ThrowUtils.throwIf(payRecordId <= 0 ,ErrorCode.PAYRECORD_NOT_FOUND);
        OrdersRecord ordersRecord = ordersRecordMapper.selectById(payRecordId);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(ordersRecord,payRecordDto);

        String qrCode = null;
        try {
            //url要可以被模拟器访问到，url为下单接口(稍后定义)
            String url = String.format(qrcodeUrl, ordersRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
            payRecordDto.setQrcode(qrCode);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GENERATING_THE_QR_CODE_ERROR);
        }

        return payRecordDto;
    }


    /**
     * 保存订单信息
     * @param userId
     * @param addOrderDto
     * @return
     */
    @Transactional
    public Orders saveOrders(String userId,AddOrderDto addOrderDto){


        //进行幂等性同一个会员类型只能有一个购买会员订单
        Orders orderByBusinessId = getOrderByBusinessId(addOrderDto.getOutBusinessId(),userId);
        if(orderByBusinessId != null){
            return orderByBusinessId;
        }
        //插入订单表，详细表
        Orders orders = new Orders();
        orders.setId(IdWorkerUtils.getInstance().nextId());
        orders.setTotalPrice(addOrderDto.getTotalPrice());
        orders.setCreateTime(LocalDateTime.now());
        orders.setStatus("600001");
        orders.setUserId(userId);
        orders.setOrderType("605001");
        orders.setOrderName(addOrderDto.getOrderName());
        orders.setOrderDescrip(addOrderDto.getOrderDescrip());
        orders.setOrderDetail(addOrderDto.getOrderDetail());
        orders.setOutBusinessId(addOrderDto.getOutBusinessId());
        int insert = ordersMapper.insert(orders);
        if(insert<=0){
            throw new BusinessException(ErrorCode.ORDER_ADD_NOT_ERROR);
        }
        Long id = orders.getId();
        //插入详细表
        List<OrdersDetail> orderDetails = null;
        try {
            String orderDetailOrJson = addOrderDto.getOrderDetail();
            orderDetails = JSON.parseArray(orderDetailOrJson, OrdersDetail.class);
            orderDetails.forEach(orderDetail -> {
                orderDetail.setOrderId(id);
            });
        }catch (Exception e){
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
        ordersDetailMapper.insertBatch(orderDetails);
        return orders;
    }

    //根据业务id查询订单
    public Orders getOrderByBusinessId(String businessId,String userId) {

//        Orders orders = null;
//        try {
//            orders = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
//                    .eq(Orders::getOutBusinessId, businessId));
//        } catch (Exception e) {
//            // 日志记录异常信息，便于后续排查
//            System.err.println("查询订单出错：" + e.getMessage());
//            // 可以根据需要进行进一步的异常处理
//        }
        Orders orders = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOutBusinessId, businessId).eq(Orders::getUserId,userId));
        return Optional.ofNullable(orders).orElse(null);
    }

    public OrdersRecord createPayRecord(Orders orders){
        if(orders==null){
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUNT);
        }
        if(orders.getStatus().equals("600002")){
            throw new BusinessException(ErrorCode.ORDER_EXIST);
        }
        OrdersRecord payRecord = new OrdersRecord();


        //生成支付交易流水号
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCreateTime(LocalDateTime.now());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setStatus("601001");
        payRecord.setUserId(Long.parseLong(orders.getUserId()));
        int insert = ordersRecordMapper.insert(payRecord);
        if(insert<=0){
            throw new BusinessException(ErrorCode.ADD_PAYRECORD_ERROR);
        }
        return payRecord;

    }
}




