package com.och.sip.transport;

import com.och.config.SipConfig;
import com.och.sip.core.codec.SipMessageEncoder;
import com.och.sip.core.codec.SipTcpDecoder;
import com.och.sip.core.codec.SipUdpDecoder;
import com.och.sip.transport.handler.SipServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SipServer {

    private ChannelFuture udpChannelFuture = null;
    private ChannelFuture tcpChannelFuture = null;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start() throws InterruptedException {
        // UDP通道配置
        Bootstrap udpBootstrap = new Bootstrap();
        udpChannelFuture = udpBootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ch.pipeline().addLast(
                                new SipUdpDecoder(),
                                new SipMessageEncoder(),
                                new LoggingHandler(),
                                new SipServerHandler()
                        );
                    }
                }).bind(SipConfig.getUdpPort()).sync();

        // TCP通道配置
        ServerBootstrap tcpBootstrap = new ServerBootstrap();
        tcpChannelFuture = tcpBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // 服务端监听通道类型
                .childHandler(new ChannelInitializer<NioSocketChannel>() { // 客户端连接通道类型
                    @Override
                    protected void initChannel(NioSocketChannel ch) { // 使用NioSocketChannel
                        ch.pipeline().addLast(
                                new SipTcpDecoder(),
                                new SipMessageEncoder(),
                                new LoggingHandler(),
                                new SipServerHandler()
                        );
                    }
                }).bind(SipConfig.getTcpPort()).sync();

        log.info("SIP Server started on port udp:{} tcp:{}", SipConfig.getUdpPort(), SipConfig.getTcpPort());
    }


    public void stop() {
        if (udpChannelFuture != null) {
            udpChannelFuture.channel().close();
        }
        if (tcpChannelFuture != null) {
            tcpChannelFuture.channel().close();
        }
    }


}
