package com.och.mrcp;

import com.och.config.MrcpConfig;
import com.och.sip.core.codec.MrcpMessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MrcpServer {

    private ChannelFuture channelFuture = null;

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            channelFuture = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new MrcpMessageCodec())
                                    .addLast(new MrcpSessionHandler());
                        }
                    }).bind(MrcpConfig.getServerPort()).sync();
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully(); // 释放资源
            workerGroup.shutdownGracefully();
            throw new RuntimeException("Connection interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            bossGroup.shutdownGracefully(); // 释放资源
            workerGroup.shutdownGracefully();
            throw new RuntimeException("Failed to connect to MRCP server: " + e.getMessage(), e);
        }
        log.info("Mrcp Server started on port  tcp:{}", MrcpConfig.getServerPort());
    }


    public void stop() {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
    }
}
