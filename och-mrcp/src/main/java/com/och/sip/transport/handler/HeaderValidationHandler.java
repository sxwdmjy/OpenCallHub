package com.och.sip.transport.handler;

import com.och.exception.InvalidSipMessageException;
import com.och.sip.core.message.SipMessage;
import io.netty.channel.ChannelHandlerContext;

public class HeaderValidationHandler implements SipMessageHandler {


    private static final String[] REQUIRED_HEADERS = {"Via", "From", "To", "Call-ID", "CSeq"};

    @Override
    public void handle(ChannelHandlerContext ctx, SipMessage message, HandlerChain chain) {
        if (!validateHeaders(message)) {
            throw new InvalidSipMessageException("Missing required headers");
        }
        chain.process(ctx, message); // 验证通过，继续传递
    }

    private boolean validateHeaders(SipMessage msg) {
        for (String header : REQUIRED_HEADERS) {
            if (msg.getHeader(header) == null) return false;
        }
        return true;
    }
}
