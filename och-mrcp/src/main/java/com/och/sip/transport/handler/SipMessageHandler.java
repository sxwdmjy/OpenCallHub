package com.och.sip.transport.handler;

import com.och.sip.core.message.SipMessage;
import io.netty.channel.ChannelHandlerContext;

public interface SipMessageHandler {

    void handle(ChannelHandlerContext ctx, SipMessage message, HandlerChain chain);
}
