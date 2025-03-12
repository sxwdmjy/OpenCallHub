package com.och.fileClient.coder;


import com.och.fileClient.protocol.CompressionType;
import com.och.fileClient.protocol.ProtocolType;
import com.och.fileClient.protocol.TransferProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProtocolDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 16) return; // type(4) + offset(8) + dataLen(4)
        int fileNameLength = in.readInt();
        String fileName = in.readCharSequence(fileNameLength, StandardCharsets.UTF_8).toString();
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
        out.add(new TransferProtocol(fileNameLength,fileName, type, offset, data, compression));
    }
}
