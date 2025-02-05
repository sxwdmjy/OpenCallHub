package com.och.sip.core.codec;

import com.och.exception.MrcpParseException;
import com.och.mrcp.MrcpMessage;
import com.och.mrcp.MrcpMessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class MrcpMessageCodec extends MessageToMessageCodec<ByteBuf, MrcpMessage> {
    private static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024; // 10MB

    @Override
    protected void encode(ChannelHandlerContext ctx, MrcpMessage msg, List<Object> out) {
        log.info("SIP message encode: " + msg);
        ByteBuf contentBuf = Unpooled.copiedBuffer(msg.toString(), StandardCharsets.UTF_8);
        ByteBuf headerBuf = ctx.alloc().buffer(4).writeInt(contentBuf.readableBytes());
        out.add(Unpooled.wrappedBuffer(headerBuf, contentBuf));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws MrcpParseException {
        String content = in.toString(StandardCharsets.UTF_8);
        log.info("Decoded SIP message from ByteBuf:\n{}", content);
        out.add(MrcpMessageParser.parse(content));
    }
}
