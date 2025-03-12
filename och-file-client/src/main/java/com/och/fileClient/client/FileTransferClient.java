package com.och.fileClient.client;

import com.och.fileClient.coder.ProtocolDecoder;
import com.och.fileClient.coder.ProtocolEncoder;
import com.och.fileClient.handler.FileReceiveHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class FileTransferClient {

    @Value("${netty.server.host:}")
    private String host;
    @Value("${netty.server.port:}")
    private int port;
    @Value("${netty.file.path:}")
    private String filePath;

    private Bootstrap bootstrap;
    private volatile Channel channel;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private ChannelPipeline pipeline;

    @PostConstruct
    public void start() {
        bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        configureClientPipeline(ch.pipeline());
                    }
                });
        connectWithRetry();
    }

    private void configureClientPipeline(ChannelPipeline pipeline) {
        this.pipeline = pipeline;
        pipeline.addLast(new LengthFieldBasedFrameDecoder(16 * 1024 * 1024, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new ProtocolDecoder());
        pipeline.addLast(new ProtocolEncoder());
        pipeline.addLast(new IdleStateHandler(0, 30, 0));
        pipeline.addLast(new FileReceiveHandler(filePath));
    }

    private void connectWithRetry() {
        while (!connected.get()) {
            try {
                ChannelFuture future = bootstrap.connect(host, port).sync();
                future.addListener(f -> {
                    if (f.isSuccess()) {
                        connected.set(true);
                        log.info("Connected to server {}:{}", host, port);
                    } else {
                        scheduleReconnect();
                    }
                });
                channel = future.channel();
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (!connected.get()) {
                    scheduleReconnect();
                }
            }
        }
    }

    private void scheduleReconnect() {
        log.warn("Will reconnect to server after 5s");
        bootstrap.config().group().schedule(this::connectWithRetry, 5, TimeUnit.SECONDS);
    }

}
