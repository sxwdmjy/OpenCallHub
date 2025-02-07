package com.och.rtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RtpHandler extends SimpleChannelInboundHandler<RtpPacket> {
    private static final int G722_PAYLOAD_TYPE = 9;
    private static final int G711_PAYLOAD_ULAW_TYPE = 0;
    private static final int G711_PAYLOAD_ALAW_TYPE = 8;
    private static final int EXPECTED_PAYLOAD_LENGTH = 160; // 实际负载，不含 RTP 头
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RtpPacket packet) {

        byte[] payload = packet.getPayload();
        int payloadLength = payload.length;

        if(packet.getPayloadType() == G722_PAYLOAD_TYPE){
            // 调用 G.722 解码器（示例中为模拟实现）
            G722Decoder decoder = new G722Decoder();
            short[] pcm = decoder.decode(payload);
            System.out.println("解码后 PCM 数据长度：" + pcm.length);
        }else if(packet.getPayloadType() == G711_PAYLOAD_ULAW_TYPE){
            short[] pcmData = G711Decoder.uDecode(payload);
            System.out.println("解码得到 PCM 样本数：" + pcmData.length);
        }else if(packet.getPayloadType() == G711_PAYLOAD_ALAW_TYPE){
            short[] pcmData = G711Decoder.aDecode(payload);
            System.out.println("解码得到 PCM 样本数：" + pcmData.length);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("RTP channel error: {}", cause.getMessage());
        ctx.close();
    }
}