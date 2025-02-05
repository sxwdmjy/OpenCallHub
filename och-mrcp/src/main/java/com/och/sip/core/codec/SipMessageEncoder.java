package com.och.sip.core.codec;

import com.och.sip.core.message.SipResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SipMessageEncoder extends MessageToMessageEncoder<SipResponse> {

    // 正则表达式匹配 SIP URI 中的 IP 和端口
    private static final Pattern FROM_PATTERN = Pattern.compile(
            "sip:(\\d+\\.\\d+\\.\\d+\\.\\d+)(?::(\\d+))?"
    );

    @Override
    protected void encode(ChannelHandlerContext ctx, SipResponse msg, List<Object> out) {
        try {
            // 1. 获取 From 头字段的值
            String fromHeader = msg.getHeader("From");
            if (fromHeader == null) {
                log.error("From header is missing");
                return;
            }

            // 2. 解析 IP 和端口
            Matcher matcher = FROM_PATTERN.matcher(fromHeader);
            if (!matcher.find()) {
                log.error("Failed to parse From header: {}", fromHeader);
                return;
            }

            String ip = matcher.group(1);
            String portStr = matcher.group(2);
            int port = (portStr != null) ? Integer.parseInt(portStr) : 5060; // 默认SIP端口

            // 3. 构建目标地址
            InetSocketAddress targetAddress = new InetSocketAddress(ip, port);

            // 4. 生成 ByteBuf 并发送
            ByteBuf buf = Unpooled.copiedBuffer(msg.toString(), CharsetUtil.UTF_8);
            if (ctx.channel() instanceof DatagramChannel) {
                out.add(new DatagramPacket(buf, targetAddress));
            } else {
                out.add(buf);
            }

        } catch (Exception e) {
            log.error("Failed to encode SIP response: {}", e.getMessage(), e);
        }
    }
}
