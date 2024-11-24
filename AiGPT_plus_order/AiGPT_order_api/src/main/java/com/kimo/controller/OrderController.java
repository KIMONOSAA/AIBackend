package com.kimo.controller;


import com.alibaba.fastjson2.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.kimo.api.client.UserClient;
import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.config.AlipayConfig;

import com.kimo.exception.ThrowUtils;

import com.kimo.model.dto.AddOrderDto;
import com.kimo.model.dto.PayRecordDto;
import com.kimo.model.dto.PayStatusDto;

import com.kimo.model.po.OrdersRecord;
import com.kimo.service.OrdersService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@Slf4j
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;

    /**
     * 生成二维码
     * @param addOrderDto
     * @param request
     * @return
     */
    @PostMapping("/generatepaycode")
//    @ResponseBody
    public BaseResponse<PayRecordDto> generatePayCode(@RequestBody AddOrderDto addOrderDto, HttpServletRequest request){
        PayRecordDto order = null;
        try {
            order = ordersService.createOrder(addOrderDto,request);
        } catch (Exception e) {
            ResultUtils.error(ErrorCode.ORDER_ADD_NOT_ERROR);
        }
        return ResultUtils.success(order);
    }

    /**
     * 扫码下单
     * @param payNo
     * @param httpResponse
     * @throws IOException
     */
    @GetMapping("/requestpay")
    public void requestPay(String payNo,HttpServletResponse httpResponse) throws IOException, AlipayApiException {

        //传入支付记录号，判断支付记录号是否存在
        OrdersRecord payRecordByPayno = ordersService.getPayRecordByPayno(payNo);
        ThrowUtils.throwIf(payRecordByPayno == null,ErrorCode.PAYRECORD_NOT_FOUND);
        ThrowUtils.throwIf(payRecordByPayno.getStatus().equals("601002"),ErrorCode.PAID_SUCCESS_NO_NEED_TO_REPAY);

        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("uezczc.natappfree.cc/order/paynotify");//在公共参数中设置回跳和通知地址
        //tjxt-user-t.itheima.net
        // 创建一个 Map 用于存储业务参数
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("out_trade_no", payNo);
        bizContentMap.put("total_amount", payRecordByPayno.getTotalPrice());
        bizContentMap.put("subject", payRecordByPayno.getOrderName());
        bizContentMap.put("product_code", "QUICK_WAP_WAY");

        // 将 Map 转换为 JSON 字符串
        String bizContentJson = JSON.toJSONString(bizContentMap);

        // 设置业务参数
        alipayRequest.setBizContent(bizContentJson);
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\""+payNo+"\"," +
//                "    \"total_amount\":"+payRecordByPayno.getTotalPrice()+"," +
//                "    \"subject\":\""+payRecordByPayno.getOrderName()+"\"," +
//                "    \"product_code\":\"QUICK_WAP_WAY\"" +
//                "  }");//填充业务参数
        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    /**
     * 查询支付结果
     * @param payNo
     * @return
     * @throws IOException
     */
    @PostMapping("/payresult")
    @ResponseBody
    public PayRecordDto payresult(String payNo,HttpServletRequest request) throws IOException {

        //查询支付结果
        return ordersService.queryPayResult(payNo,request);

    }


    /**
     * 支付通知
     * @param request
     * @throws UnsupportedEncodingException
     * @throws AlipayApiException
     */
    @PostMapping("/paynotify")
    public void paynotify(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {

//        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
//        UserDto userDto = userClient.GobalGetLoginUser(username);
//        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = 0L;
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        //验签
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if(verify_result) {//验证成功

            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            //appid
            String app_id = new String(request.getParameter("app_id").getBytes("ISO-8859-1"),"UTF-8");
            //total_amount
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            //交易成功处理
            if (trade_status.equals("TRADE_SUCCESS")) {

                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setApp_id(app_id);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTotal_amount(total_amount);

                //处理逻辑。。。
                ordersService.saveAliPayStatus(payStatusDto,userId);
            }
        }


    }

}
