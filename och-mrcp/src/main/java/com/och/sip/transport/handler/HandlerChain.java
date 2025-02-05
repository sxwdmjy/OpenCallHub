package com.och.sip.transport.handler;

import com.och.sip.core.message.SipMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class HandlerChain {
    private final List<SipMessageHandler> handlers = new ArrayList<>();
    private int currentIndex = 0;

    public void process(ChannelHandlerContext ctx, SipMessage msg) {
        if (currentIndex < handlers.size()) {
            SipMessageHandler handler = handlers.get(currentIndex++);
            handler.handle(ctx, msg, this);
        }
    }

    public void addHandler(SipMessageHandler handler) {
        handlers.add(handler);
    }

    public void reset() {
        currentIndex = 0;
    }
}
