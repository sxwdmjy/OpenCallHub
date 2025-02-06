package com.och.mrcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MrcpSessionHandler extends ChannelInboundHandlerAdapter {
    private final MrcpSessionManager sessionManager = MrcpSessionManager.getInstance();


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String sessionId = getSessionIdFromChannel(ctx.channel());
        MrcpSession session = sessionManager.getSession(sessionId);
        if (session != null){
            session.transitionState(MrcpSession.State.ACTIVE);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof MrcpMessage) {
            MrcpMessage message = (MrcpMessage) msg;
            String channelId = message.getHeader("Channel-Identifier");

            if (msg instanceof MrcpRequest) {
                MrcpRequest req = (MrcpRequest) msg;
                if ("TEARDOWN".equals(req.getMethod())) {
                    // 处理MRCP协议终止请求
                    sessionManager.destroySession(getSessionIdFromChannel(ctx.channel()));
                }
            }
            // 通过会话管理器获取会话
            MrcpSession session = sessionManager.getSession(channelId);
            if (session == null) {
                log.error("No session found for Channel-Identifier: {}", channelId);
                sendErrorResponse(message, ctx, 481, "Session Not Found");
                return;
            }

            try {
                session.process(message);
            } catch (Exception e) {
                log.error("Failed to process MRCP message: {}", e.getMessage());
                session.transitionState(MrcpSession.State.TERMINATING);
            }
        } else {
            log.warn("Received unsupported message type: {}", msg.getClass().getName());
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("MRCP session error: {}", cause.getMessage());
        ctx.close().addListener(future -> {
            if (future.isSuccess()) {
                String sessionId = getSessionIdFromChannel(ctx.channel());
                sessionManager.destroySession(sessionId);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = getSessionIdFromChannel(ctx.channel());
        MrcpSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            session.transitionState(MrcpSession.State.CLOSED);
            sessionManager.destroySession(sessionId);
            log.info("MRCP session closed: {}", sessionId);
        }
    }

    // ================= 工具方法 =================

    private String getSessionIdFromChannel(Channel channel) {
        // 假设会话ID存储在Channel属性中（需在channelActive时设置）
        if (StringUtil.isNullOrEmpty(channel.attr(MrcpSession.SESSION_ID_ATTRIBUTE_KEY).get())){
          return  channel.parent().attr(MrcpSession.SESSION_ID_ATTRIBUTE_KEY).get();
        }else return channel.attr(MrcpSession.SESSION_ID_ATTRIBUTE_KEY).get();
    }

    private void sendErrorResponse(MrcpMessage msg, ChannelHandlerContext ctx, int code, String reason) {
        if (msg instanceof MrcpRequest) {
            MrcpRequest req = (MrcpRequest) msg;
            MrcpResponse res = new MrcpResponse();
            res.setRequestId(req.getRequestId());
            res.setStatusCode(code);
            res.addHeader("Completion-Cause", "002 error");
            res.setBody(reason);
            // 通过 ChannelHandlerContext 发送响应
            ctx.channel().writeAndFlush(res);
        }
    }

}
