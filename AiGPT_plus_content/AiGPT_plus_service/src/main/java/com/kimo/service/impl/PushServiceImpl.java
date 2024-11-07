//package com.kimo.service.impl;
//
//import com.alibaba.fastjson2.JSON;
//import com.kimo.common.*;
//import com.kimo.constant.ExceptionConstant;
//import com.kimo.xunfei.config.XFConfig;
//import com.kimo.xunfei.constant.RoleContent;
//import com.kimo.xunfei.listener.XFWebClient;
//import com.kimo.xunfei.listener.XFWebSocketListener;
//import com.kimo.service.PushService;
//import io.netty.channel.Channel;
//import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.WebSocket;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Slf4j
//@Service
//public class PushServiceImpl implements PushService {
//
//    @Autowired
//    private XFConfig xfConfig;
//
//    @Autowired
//    private XFWebClient xfWebClient;
//
//
//    /**
//     * 向指定用户的 WebSocket 频道发送消息。
//     *
//     * 该方法通过用户 ID 找到对应的 WebSocket 频道，并发送给定的文本消息。如果用户 ID 或文本为空，将抛出运行时异常。
//     *
//     * @param uid 用户 ID，标识要发送消息的目标用户。
//     * @param text 要发送的文本消息。
//     * @throws RuntimeException 如果 uid 或 text 为空。
//     */
//    @Override
//    public void pushToOne(String uid, String text) {
//        if (StringUtils.isEmpty(uid) || StringUtils.isEmpty(text)) {
//            log.error("uid或text均不能为空");
//            throw new RuntimeException(ExceptionConstant.NO_NULL);
//        }
//        ConcurrentHashMap<String, Channel> userChannelMap = NettyGroup.getUserChannelMap();
//        for (String channelId : userChannelMap.keySet()) {
//            if (channelId.equals(uid)) {
//                Channel channel = userChannelMap.get(channelId);
//                if (channel != null) {
//                    BaseResponse success = ResultUtils.success(text);
//                    channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(success)));
//                    log.info("信息发送成功：{}", JSON.toJSONString(success));
//                } else {
//                    log.error("该id对于channelId不存在！");
//                }
//                return;
//            }
//        }
//        log.error("该用户不存在！");
//    }
//
//
//    /**
//     * 向所有连接的 WebSocket 客户端推送消息。
//     *
//     * 该方法将给定的文本消息转换为成功响应并通过所有活动的 WebSocket 频道发送该消息。
//     *
//     * @param text 要发送的文本消息，发送前会去除首尾空白字符。
//     */
//    @Override
//    public void pushToAll(String text) {
//        String trim = text.trim();
//        BaseResponse success = ResultUtils.success(trim);
//        NettyGroup.getChannelGroup().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(success)));
//        log.info("信息推送成功：{}", JSON.toJSONString(success));
//    }
//
//    //测试账号只有2个并发，此处只使用一个，若是生产环境允许多个并发，可以采用分布式锁 使用synchronized是为了保证可见性因为
//    //在触发xfWebSocketListener监听的时候监听玩会把xfWebSocketListener里的isWsCloseFlag变为true但是
//    //在jvm里面其实每个线程都有自己私有内存，这个线程把xfWebSocketListener里的isWsCloseFlag变为true后会把这个数据保存到自己本地的内存
//    //不会立即把这个数据保存到共享的堆的主存中所以导致这个线程执行到 if (xfWebSocketListener.isWsCloseFlag()) 这句的时候他有可能还是为False
//    @Override
//    public synchronized BaseResponse pushMessageToXFServer(String uid, String text) {
//        RoleContent userRoleContent = RoleContent.createUserRoleContent(text);
//        ArrayList<RoleContent> questions = new ArrayList<>();
//        questions.add(userRoleContent);
//        XFWebSocketListener xfWebSocketListener = new XFWebSocketListener();
//        WebSocket webSocket = xfWebClient.sendMsg(uid, questions, xfWebSocketListener);
//        if (webSocket == null) {
//            log.error("webSocket连接异常");
//            ResultUtils.error(ErrorCode.OPERATION_ERROR,"请求异常，请联系管理员");
//        }
//        try {
//            int count = 0;
//            int maxCount = xfConfig.getMaxResponseTime() * 5;
//            while (count <= maxCount) {
//                Thread.sleep(200);
//                if (xfWebSocketListener.isWsCloseFlag()) {
//                    break;
//                }
//                count++;
//            }
//            if (count > maxCount) {
//                return ResultUtils.error(ErrorCode.OPERATION_ERROR,"响应超时，请联系相关人员");
//            }
//            return ResultUtils.success(xfWebSocketListener.getAnswer());
//        } catch (Exception e) {
//            log.error("请求异常：{}", e);
//        } finally {
//            webSocket.close(1000, "");
//        }
//        return ResultUtils.success("");
//    }
//}