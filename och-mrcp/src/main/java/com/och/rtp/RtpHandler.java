package com.och.rtp;

import com.och.engine.AsrEngine;
import com.och.engine.EngineFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class RtpHandler extends SimpleChannelInboundHandler<RtpPacket> {
    private static final int G722_PAYLOAD_TYPE = 9;
    private static final int G711_PAYLOAD_ULAW_TYPE = 0;
    private static final int G711_PAYLOAD_ALAW_TYPE = 8;

    private final AsrEngine asrEngine;


    public RtpHandler(AsrEngine asrEngine) {
        this.asrEngine = asrEngine;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RtpPacket packet) {

        byte[] payload = packet.getPayload();
        int payloadLength = payload.length;
        short[] pcmData = null;
        if (packet.getPayloadType() == G722_PAYLOAD_TYPE) {
            // 调用 G.722 解码器（示例中为模拟实现）
            G722Decoder decoder = new G722Decoder();
            pcmData = decoder.decode(payload);
            System.out.println("解码后 PCM 数据长度：" + pcmData.length);
        } else if (packet.getPayloadType() == G711_PAYLOAD_ULAW_TYPE) {
            pcmData = G711Decoder.uDecode(payload);
            System.out.println("解码得到 PCM 样本数：" + pcmData.length);
        } else if (packet.getPayloadType() == G711_PAYLOAD_ALAW_TYPE) {
            pcmData = G711Decoder.aDecode(payload);
            System.out.println("解码得到 PCM 样本数：" + pcmData.length);
        }
        if (pcmData != null) {
            asrEngine.recognize(processPCMData(pcmData));
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("RTP channel error: {}", cause.getMessage());
        ctx.close();
    }

    private byte[] processPCMData(short[] pcmData) {
        byte[] pcmBytes = new byte[pcmData.length * 2];
        for (int i = 0; i < pcmData.length; i++) {
            pcmBytes[i * 2] = (byte) (pcmData[i] & 0xFF);
            pcmBytes[i * 2 + 1] = (byte) ((pcmData[i] >> 8) & 0xFF);
        }
        log.debug("发送处理PCM后数据bytes:{}", pcmBytes.length);
        return pcmBytes;
    }
}