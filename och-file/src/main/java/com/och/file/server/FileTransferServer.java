package com.och.file.server;

import com.och.file.server.coder.ProtocolDecoder;
import com.och.file.server.coder.ProtocolEncoder;
import com.och.file.server.protocol.CompressionType;
import com.och.file.server.protocol.ProtocolType;
import com.och.file.server.protocol.TransferProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@Slf4j
public class FileTransferServer {

    private final NettyConfig config;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ChannelFuture channelFuture = null;

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public FileTransferServer(NettyConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(config.getBossThreads());
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads());

        channelFuture = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        configurePipeline(ch.pipeline());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .bind(config.getPort())
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("Netty server started on port {}", config.getPort());
                    } else {
                        log.error("Failed to start netty server", future.cause());
                    }
                });
    }

    private void configurePipeline(ChannelPipeline pipeline) {
        // 流量整形 (1MB/s)
        pipeline.addLast(new ChannelTrafficShapingHandler(1024 * 1024, 1024 * 1024));

        // 协议帧处理
        pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMaxContentLength(), 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));

        // 压缩处理
        //pipeline.addLast(new CompressionNegotiationHandler());

        pipeline.addLast(new TrafficControlHandler());

        // 协议编解码
        pipeline.addLast(new ProtocolEncoder());
        pipeline.addLast(new ProtocolDecoder());

        // 业务逻辑
        pipeline.addLast(new IdleStateHandler(60, 0, 0));
        pipeline.addLast(new FileTransferHandler(channelGroup));
    }

    public void sendFileToClient(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileSize = raf.length();
            byte[] fileData = new byte[(int) raf.length()];
            raf.readFully(fileData);  // 一次性读取全部字节
            // 发送文件元数据
            TransferProtocol meta = new TransferProtocol(
                    file.getName().length(),
                    file.getName(),
                    ProtocolType.FILE_TRANSFER,
                    fileSize,
                    fileData,
                    CompressionType.NONE
            );
            channelGroup.writeAndFlush(meta);
        } catch (Exception e) {
            log.error("File transfer failed: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if(channelFuture != null){
            channelFuture.channel().close();
        }
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
    }


    public void sendFileToClient(String fileName, String filePath) {
        if (fileName != null && filePath != null){
            if(isFileURL(filePath)){
                try {
                    File file = new File(fileName);
                    FileUtils.copyURLToFile(new URL(filePath), file, 10 * 1000, 10 * 1000);
                    System.out.println("文件下载成功");
                    //sendFileToClient(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else {
                try {
                    File file = new File(fileName);
                    FileUtils.copyFile(new File(filePath), file);
                    System.out.println("文件下载成功");
                  //  sendFileToClient(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static boolean isFileURL(String path) {
        try {
            new URL(path);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
