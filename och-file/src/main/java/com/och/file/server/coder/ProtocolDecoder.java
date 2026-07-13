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
    /**
     * 解码字节流为 TransferProtocol 对象
     * <p>协议格式：fileNameLength(4B) + fileName(NB) + type(4B) + offset(8B) + dataLen(4B) + data(NB) + compression(4B)</p>
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 16) return; // fileNameLength(4) + type(4) + offset(8) = 16
        in.markReaderIndex();
        int fileNameLength = in.readInt();
        if (in.readableBytes() < fileNameLength + 12) { // fileName + type(4) + offset(8)
            in.resetReaderIndex();
            return;
        }
        String fileName = in.readCharSequence(fileNameLength, StandardCharsets.UTF_8).toString();
        int typeValue = in.readInt();
        ProtocolType type = ProtocolType.valueOf(typeValue);
        long offset = in.readLong();
        int dataLen = in.readInt();

        if (in.readableBytes() < dataLen + 4) { // data + compression(4)
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLen];
        in.readBytes(data);
        int compressionValue = in.readInt();
        CompressionType compression = CompressionType.valueOf(compressionValue);
        out.add(new TransferProtocol(fileNameLength, fileName, type, offset, data, compression));
    }
}
