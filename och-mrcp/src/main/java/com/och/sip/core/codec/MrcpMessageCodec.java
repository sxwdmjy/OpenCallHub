package com.och.sip.core.codec;

import com.och.exception.MrcpParseException;
import com.och.mrcp.MrcpMessage;
import com.och.mrcp.MrcpMessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MrcpMessageCodec extends MessageToMessageCodec<ByteBuf, MrcpMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MrcpMessage msg, List<Object> out) {
        log.debug("Encoding MRCP message: {}", msg);

        byte[] contentBytes = msg.toString().getBytes(StandardCharsets.UTF_8);
        int contentLength = contentBytes.length;
        ByteBuf buffer = ctx.alloc().buffer(contentBytes.length);
        buffer.writeBytes(contentBytes);
        out.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws MrcpParseException {
        String rawMessage = in.toString(StandardCharsets.UTF_8); // 直接解析为字符串
        log.debug("Decoded raw MRCP message:\n{}", rawMessage);
        out.add(MrcpMessageParser.parse(rawMessage));
    }

    private int parseContentLength(String rawMessage) {
        Pattern pattern = Pattern.compile("Content-Length:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rawMessage);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1; // Content-Length 不存在
    }
}
