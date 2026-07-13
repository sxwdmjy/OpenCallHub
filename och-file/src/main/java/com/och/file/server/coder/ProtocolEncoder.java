package com.och.file.server.coder;

import com.och.file.server.protocol.TransferProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 传输协议编码器
 * <p>将 TransferProtocol 对象编码为字节流，格式：fileNameLength(4B) + fileName(NB) + type(4B) + offset(8B) + dataLen(4B) + data(NB) + compression(4B)。</p>
 */
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
