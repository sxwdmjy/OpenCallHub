package com.och.rtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class RtpDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket packet, List<Object> out) throws Exception {
        ByteBuf content = packet.content();

        // 检查 RTP 包的最小长度
        if (content.readableBytes() < 12) {
            return; // RTP 头部至少需要 12 字节
        }

        // 解析 RTP 头部
        int version = (content.getByte(0) & 0xC0) >> 6; // RTP 版本
        if (version != 2) {
            throw new IllegalArgumentException("Unsupported RTP version: " + version);
        }

        boolean padding = (content.getByte(0) & 0x20) != 0;  // Padding 位
        boolean extension = (content.getByte(0) & 0x10) != 0; // Extension 位
        int csrcCount = content.getByte(0) & 0x0F;           // CSRC 个数
        boolean marker = (content.getByte(1) & 0x80) != 0;   // Marker 位
        int payloadType = content.getByte(1) & 0x7F;         // Payload 类型
        int sequenceNumber = content.getUnsignedShort(2);    // 序列号
        long timestamp = content.getUnsignedInt(4);          // 时间戳
        long ssrc = content.getUnsignedInt(8);               // SSRC

        // 跳过 CSRC 列表
        content.skipBytes(12 + 4 * csrcCount);

        // 提取 RTP 负载
        byte[] payload = new byte[content.readableBytes()];
        content.readBytes(payload);

        // 将 RTP 数据封装为 RTP 包对象
        RtpPacket rtpPacket = new RtpPacket(version, padding, extension, csrcCount, marker, payloadType, sequenceNumber, timestamp, ssrc, payload);
        out.add(rtpPacket);
    }
}
