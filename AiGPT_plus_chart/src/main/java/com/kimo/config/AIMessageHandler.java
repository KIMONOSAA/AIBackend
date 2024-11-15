package com.kimo.config;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentHashMap;

public class AIMessageHandler extends SimpleChannelInboundHandler<String> {

    // 存储每个客户端的 Channel，根据用户 ID 进行映射
    private static final ConcurrentHashMap<String, ChannelHandlerContext> clients = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 当有客户端连接时，等待它发送用户的唯一标识符
        System.out.println("等待客户端发送唯一标识符...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 在连接时，客户端应该首先发送一个唯一标识符（例如用户 ID）
        if (!clients.containsValue(ctx)) {
            // 如果当前通道未被标记为用户 ID，假设第一个消息是用户的唯一 ID
            String clientId = msg.trim(); // 假设收到的第一个消息是用户的 ID
            clients.put(clientId, ctx);
            System.out.println("客户端注册成功，用户 ID: " + clientId);
        } else {
            // 处理已注册的客户端消息
            System.out.println("收到来自用户的消息: " + msg);
            // 这里可以根据需求处理客户端的请求
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 当有客户端断开连接时，从映射中移除
        clients.values().remove(ctx);
        System.out.println("客户端断开连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 处理异常
        cause.printStackTrace();
        ctx.close();
    }

    // 方法：将 AI 消息发送给指定用户
    public static void sendMessageToUser(String clientId, String message) {
        ChannelHandlerContext ctx = clients.get(clientId);
        if (ctx != null) {
            ctx.writeAndFlush(message + "\n");
        } else {
            System.out.println("用户 ID: " + clientId + " 未找到连接");
        }
    }
}
