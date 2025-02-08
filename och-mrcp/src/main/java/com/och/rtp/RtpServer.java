package com.och.rtp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RtpServer {

    private final int port;
    private Channel channel;

    public RtpServer(int port) {
        this.port = port;
    }

    public void start(String mrcpSessionId) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            // 添加RTP处理逻辑（例如转发到媒体处理器）
                            ch.pipeline().addLast(new RtpDecoder(),new RtpHandler(mrcpSessionId));
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
}
