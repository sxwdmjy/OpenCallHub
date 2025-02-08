package com.och.mrcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MrcpSessionHandler extends ChannelInboundHandlerAdapter {
    private final MrcpSessionManager sessionManager = MrcpSessionManager.getInstance();



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof MrcpMessage) {
            MrcpMessage message = (MrcpMessage) msg;
            String channelId = message.getHeader("Channel-Identifier");

            if (msg instanceof MrcpRequest) {
                MrcpRequest req = (MrcpRequest) msg;
                if ("TEARDOWN".equals(req.getMethod())) {
                    // 处理MRCP协议终止请求
                    sessionManager.destroySession("");
                }
            }
            // 通过会话管理器获取会话
            MrcpSession session = sessionManager.getSession(channelId);
            if (session == null) {
                log.error("No session found for Channel-Identifier: {}", channelId);
                sendErrorResponse(message, ctx, 481, "Session Not Found");
                return;
            }
            session.setChannel(ctx.channel());
            if(session.getState().get() == MrcpSession.State.CONNECTING){
                session.transitionState(MrcpSession.State.ACTIVE);
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
                String sessionId = "";
                sessionManager.destroySession(sessionId);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = "";
        MrcpSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            session.transitionState(MrcpSession.State.CLOSED);
            sessionManager.destroySession(sessionId);
            log.info("MRCP session closed: {}", sessionId);
        }
    }


    private void sendErrorResponse(MrcpMessage msg, ChannelHandlerContext ctx, int code, String reason) {
        if (msg instanceof MrcpRequest) {
            MrcpRequest req = (MrcpRequest) msg;
            MrcpResponse res = new MrcpResponse();
            res.setVersion(req.getVersion());
            res.setMessageLength(reason.length());
            res.setRequestId(req.getRequestId());
            res.setStatusText("COMPLETE");
            res.setStatusCode(code);
            res.addHeader("Completion-Cause", "002 error");
            res.addHeader("Content-Length", String.valueOf(reason.length()));
            res.setBody(reason);
            ctx.channel().writeAndFlush(res);
        }
    }

}
