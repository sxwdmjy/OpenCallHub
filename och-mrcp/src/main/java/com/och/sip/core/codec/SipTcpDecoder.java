package com.och.sip.core.codec;

import com.och.sip.core.message.SipMessage;
import com.och.sip.core.message.SipRequest;
import com.och.sip.core.message.SipResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.util.Recycler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SipTcpDecoder extends ByteToMessageDecoder {

    private final Recycler<SipRequest> requestRecycler = new Recycler<SipRequest>() {
        @Override
        protected SipRequest newObject(Recycler.Handle<SipRequest> handle) {
            return new SipRequest(handle);
        }
    };

    private final Recycler<SipResponse> responseRecycler = new Recycler<SipResponse>() {
        @Override
        protected SipResponse newObject(Recycler.Handle<SipResponse> handle) {
            return new SipResponse(handle);
        }
    };

    private static final Pattern START_LINE_PATTERN =
            Pattern.compile("^(\\w+) (\\S+) SIP/2.0$|^SIP/2.0 (\\d{3}) (.*)$");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            boolean isRequest = isRequest(in);
            SipMessage msg = createMessage(isRequest);
            parseMessage(in, msg, isRequest);
            out.add(msg);
        } catch (Exception e) {
            in.skipBytes(in.readableBytes());
            throw new DecoderException("SIP message decode failed", e);
        }
    }


    private boolean isRequest(ByteBuf in) {
        if (in.readableBytes() < 4) return false;
        byte[] prefix = new byte[4];
        in.getBytes(in.readerIndex(), prefix);
        return !(prefix[0] == 'S' && prefix[1] == 'I' && prefix[2] == 'P' && prefix[3] == '/');
    }

    private SipMessage createMessage(boolean isRequest) {
        return isRequest ?
                requestRecycler.get() :
                responseRecycler.get();
    }


    private void parseMessage(ByteBuf in, SipMessage msg, boolean isRequest) {
        String content = in.toString(StandardCharsets.UTF_8);
        String[] parts = content.split("\r\n\r\n", 2);
        String[] headers = parts[0].split("\r\n");

        // 解析起始行
        Matcher matcher = START_LINE_PATTERN.matcher(headers[0]);
        if (!matcher.find()) {
            throw new DecoderException("Invalid SIP start line: " + headers[0]);
        }

        if (isRequest) {
            SipRequest req = (SipRequest) msg;
            req.setMethod(matcher.group(1))
                    .setUri(matcher.group(2));
        } else {
            SipResponse res = (SipResponse) msg;
            res.setStatusCode(Integer.parseInt(matcher.group(3)))
                    .setReasonPhrase(matcher.group(4));
        }

        // 解析头字段
        for (int i = 1; i < headers.length; i++) {
            String[] headerParts = headers[i].split(":", 2);
            if (headerParts.length == 2) {
                msg.addHeader(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        // 处理消息体
        if (parts.length > 1) {
            msg.setBody(parts[1]);
        }

        in.readerIndex(in.writerIndex());
    }
}