package com.och.file.server.coder;

import com.och.file.server.protocol.TransferProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtocolEncoder extends MessageToByteEncoder<TransferProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, TransferProtocol msg, ByteBuf out) {
        out.writeInt(msg.getFileNameLength());
        out.writeBytes(msg.getFileName().getBytes());
        out.writeInt(msg.getType().getValue());
        out.writeLong(msg.getOffset());
        out.writeInt(msg.getData().length);
        out.writeBytes(msg.getData());
        out.writeInt(msg.getCompression().getValue());
    }
}
