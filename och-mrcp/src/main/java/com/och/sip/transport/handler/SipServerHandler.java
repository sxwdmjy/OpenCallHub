package com.och.sip.transport.handler;

import com.och.sip.core.message.SipMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SipServerHandler extends SimpleChannelInboundHandler<SipMessage> {
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final HandlerChain chain;

    public SipServerHandler() {
        chain = new HandlerChain();
        chain.addHandler(new HeaderValidationHandler());
        chain.addHandler(new TransactionHandler());
        chain.addHandler(new DialogHandler());
        chain.addHandler(new SdpNegotiationHandler());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SipMessage msg) {
        chain.reset();
        chain.process(ctx, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channels.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Channel error: {}", cause.getMessage(),cause);
        channels.remove(ctx.channel());
        ctx.close();
    }
}
