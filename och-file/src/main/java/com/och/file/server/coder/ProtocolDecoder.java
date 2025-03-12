package com.och.file.server.coder;


import com.och.file.server.protocol.CompressionType;
import com.och.file.server.protocol.ProtocolType;
import com.och.file.server.protocol.TransferProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProtocolDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 16) return; // type(4) + offset(8) + dataLen(4)
        String fileName = in.readCharSequence(8, StandardCharsets.UTF_8).toString();
        int typeValue = in.readInt();
        ProtocolType type = ProtocolType.valueOf(typeValue);
        long offset = in.readLong();
        int dataLen = in.readInt();

        if (in.readableBytes() < dataLen) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLen];
        in.readBytes(data);
        int compressionValue = in.readInt();
        CompressionType compression = CompressionType.valueOf(compressionValue);
        out.add(new TransferProtocol(fileName.length(),fileName,type, offset, data,compression));
    }
}
