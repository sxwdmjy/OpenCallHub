package com.och.file.server;

import com.och.file.server.protocol.CompressionType;
import com.och.file.server.protocol.ProtocolType;
import com.och.file.server.protocol.TransferProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;

@Slf4j
public class FileTransferHandler extends SimpleChannelInboundHandler<TransferProtocol> {

    private final ChannelGroup channelGroup;


    public FileTransferHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active: {}", ctx.channel());
        channelGroup.add(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransferProtocol msg) throws Exception {
        switch (msg.getType()) {
            case ACK:
                break;
            case ERROR:
                break;
            default:
                break;
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.remove(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("Channel error: {}", cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
