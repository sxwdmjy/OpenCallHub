package com.och.rtp;

import cn.hutool.core.collection.CollectionUtil;
import com.och.engine.AsrEngine;
import com.och.engine.EngineFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
public class RtpServer {

    private final int port;
    private Channel channel;

    private final AsrEngine asrEngine;


    public RtpServer(int port) {
        this.port = port;
        this.asrEngine = EngineFactory.getAsrEngine("aliyun");
    }

    public void start() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            // 添加RTP处理逻辑（例如转发到媒体处理器）
                            ch.pipeline().addLast(new RtpDecoder(),new RtpHandler(asrEngine));
                        }
                    });
            channel = bootstrap.bind(port).sync().channel();
            log.info("RTP server started on port: {}", port);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(group::shutdownGracefully));
        }
    }

    public void stop() {
        if (channel != null) {
            channel.close();
            log.info("RTP server stopped on port: {}", port);
        }
    }

    /**
     * 发送RTP音频数据
     * @param audioData
     * @param rtpEndpoint
     */
    public void sendRtpAudio(byte[] audioData, String rtpEndpoint){
        RtpEncoder rtpEncoder = new RtpEncoder(parseRtpEndpoint(rtpEndpoint));
        channel.pipeline().addLast(rtpEncoder);
        List<RtpPacket> rtpPackets = rtpEncoder.encodePCMToRtp(audioData);
        if (CollectionUtil.isNotEmpty(rtpPackets)){
            for (RtpPacket rtpPacket : rtpPackets) {
                channel.writeAndFlush(rtpPacket);
            }
        }
    }


    /**
     * 解析RTP终端地址
     * @param rtpEndpoint 地址字符串
     * @return InetSocketAddress 对象
     * @throws IllegalArgumentException 格式错误时抛出
     */
    public static InetSocketAddress parseRtpEndpoint(String rtpEndpoint) {
        try {
            // 统一添加协议头方便URI解析
            if (!rtpEndpoint.startsWith("rtp://") && !rtpEndpoint.startsWith("udp://")) {
                rtpEndpoint = "rtp://" + rtpEndpoint;
            }

            URI uri = new URI(rtpEndpoint);

            // 验证协议
            if (!"rtp".equals(uri.getScheme()) && !"udp".equals(uri.getScheme())) {
                throw new IllegalArgumentException("Invalid protocol, expected rtp/udp: " + uri.getScheme());
            }

            // 处理IPv6地址（URI会将[2001:db8::1]转换为2001:db8::1）
            String host = uri.getHost();
            int port = uri.getPort();

            if (host == null || port == -1) {
                throw new IllegalArgumentException("Missing host or port");
            }

            return new InetSocketAddress(host, port);

        } catch (URISyntaxException e) {
            // 回退处理无协议头的IPv6地址（如 [2001::1]:5004）
            if (rtpEndpoint.startsWith("[")) {
                int endBracket = rtpEndpoint.indexOf(']');
                if (endBracket != -1 && rtpEndpoint.indexOf(':', endBracket) != -1) {
                    String host = rtpEndpoint.substring(1, endBracket);
                    String portStr = rtpEndpoint.substring(endBracket + 2);
                    try {
                        int port = Integer.parseInt(portStr);
                        return new InetSocketAddress(host, port);
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("Invalid port number", ex);
                    }
                }
            }

            // 尝试直接解析为 host:port
            String[] parts = rtpEndpoint.split(":");
            if (parts.length == 2) {
                try {
                    int port = Integer.parseInt(parts[1]);
                    return new InetSocketAddress(parts[0], port);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid port format", ex);
                }
            }

            throw new IllegalArgumentException("Invalid RTP endpoint format: " + rtpEndpoint, e);
        }
    }

}
