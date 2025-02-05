package com.och.rtp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RtpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        String sender = packet.sender().getHostString() + ":" + packet.sender().getPort();
        int dataLength = packet.content().readableBytes();
        log.info("Received RTP packet from {} (size: {} bytes)", sender, dataLength);
       // MediaProcessor.processRtpData(packet.content());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("RTP channel error: {}", cause.getMessage());
        ctx.close();
    }
}