package com.och.rtp;

import com.och.utils.G711Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RtpEncoder extends MessageToMessageEncoder<RtpPacket> {

    private static final int RTP_VERSION = 2;
    private static final boolean PADDING = false;
    private static final boolean EXTENSION = false;
    private static final int CSRC_COUNT = 0;
    private static final boolean MARKER = false;
    private static final int PAYLOAD_TYPE = 0; // G.711 u-law
    private static final int RTP_TIMESTAMP_INCREMENT = 160; // 对于8kHz采样率，每包20ms数据
    private static final int RTP_PACKET_SIZE = 160; // 每个RTP包的载荷大小

    private int sequenceNumber;
    private final AtomicInteger timestamp = new AtomicInteger(new SecureRandom().nextInt());
    private final int ssrc;
    private final InetSocketAddress targetAddress;

    /**
     * 构造方法，传入目标地址
     * @param targetAddress 目标RTP接收端地址
     */
    public RtpEncoder(InetSocketAddress targetAddress) {
        this.targetAddress = targetAddress;
        this.sequenceNumber = (int) (Math.random() * 0xFFFF);
        this.ssrc = generateSsrc();
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, RtpPacket msg, List<Object> out) throws Exception {
        byte[] packetBytes = serializeRtpPacket(msg);
        ByteBuf buf = Unpooled.wrappedBuffer(packetBytes);
        out.add(new DatagramPacket(buf, targetAddress));
    }

    /**
     * 将PCM数据转换为RTP包列表：
     * @param pcmData PCM格式的音频数据
     * @return 封装RTP包的列表，若无有效数据则返回空列表
     */
    public List<RtpPacket> encodePCMToRtp(byte[] pcmData) {
        byte[] processedPCM  = removeSilence(pcmData, 50);
        if (processedPCM  == null || processedPCM .length == 0) {
            return Collections.emptyList();
        }
        byte[] ulawData = G711Utils.pcmToULaw(processedPCM );
        List<RtpPacket> packets = new ArrayList<>();
        int offset = 0;
        // 3. 分片封装为RTP包
        while (offset < ulawData .length) {
            int frameSize = Math.min(RTP_PACKET_SIZE, ulawData .length - offset);
            byte[] payload = Arrays.copyOfRange(ulawData , offset, offset + frameSize);
            RtpPacket packet = new RtpPacket(
                    RTP_VERSION, PADDING, EXTENSION, CSRC_COUNT, MARKER,
                    PAYLOAD_TYPE, sequenceNumber, timestamp.get(), ssrc, payload
            );
            packets.add(packet);
            sequenceNumber = (sequenceNumber + 1) & 0xFFFF;
            timestamp.getAndAdd(RTP_TIMESTAMP_INCREMENT);
            offset += frameSize;
        }
        return packets;
    }

    /**
     * 将RtpPacket对象序列化为字节数组，格式为：
     * 12字节RTP头（包含版本、标志、序列号、时间戳、SSRC等）+ 载荷数据
     *
     * @param packet RtpPacket对象
     * @return 序列化后的字节数组
     */
    public static byte[] serializeRtpPacket(RtpPacket packet) {
        int headerSize = 12;
        ByteBuffer buffer = ByteBuffer.allocate(headerSize + packet.getPayload().length);
        // 第一字节：版本、padding、扩展、CSRC计数
        int b1 = (packet.getVersion() & 0x03) << 6;
        if (packet.isPadding()) {
            b1 |= 0x20;
        }
        if (packet.isExtension()) {
            b1 |= 0x10;
        }
        b1 |= (packet.getCsrcCount() & 0x0F);
        buffer.put((byte) b1);
        // 第二字节：marker位和负载类型
        int b2 = (packet.isMarker() ? 0x80 : 0) | (packet.getPayloadType() & 0x7F);
        buffer.put((byte) b2);
        // 序列号（2字节）、时间戳（4字节）、SSRC（4字节）
        buffer.putShort((short) packet.getSequenceNumber());
        buffer.putInt((int) packet.getTimestamp());
        buffer.putInt((int) packet.getSsrc());
        // 载荷数据
        buffer.put(packet.getPayload());
        return buffer.array();
    }

    /**
     * 生成SSRC，采用随机数和计数器组合的方式
     */
    public static int generateSsrc() {
        SecureRandom random = new SecureRandom();
        int randomPart = random.nextInt() & 0xFFFF0000;
        int counterPart = random.nextInt() & 0x0000FFFF;
        return randomPart | counterPart;
    }

    /**
     * 去除u-law编码数据中前后部分的静音段，避免发送无效数据
     *
     * @param pcmData   u-law编码后的数据
     * @param threshold 静音判断阈值
     * @return 去除静音后的数据
     */
    public byte[] removeSilence(byte[] pcmData, int threshold) {
        int start = 0, end = pcmData.length - 1;
        while (start < end) {
            if (start + 1 >= pcmData.length) break;
            short sample = (short) (((pcmData[start + 1] & 0xFF) << 8) | (pcmData[start] & 0xFF));
            if (Math.abs(sample) > threshold) break;
            start += 2;
        }
        while (end > start) {
            if (end - 1 < 0) break;
            short sample = (short) (((pcmData[end] & 0xFF) << 8) | (pcmData[end - 1] & 0xFF));
            if (Math.abs(sample) > threshold) break;
            end -= 2;
        }
        return Arrays.copyOfRange(pcmData, start, end + 1);
    }
}
