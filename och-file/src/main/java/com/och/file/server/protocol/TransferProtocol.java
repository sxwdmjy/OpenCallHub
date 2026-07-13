package com.och.file.server.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文件传输协议
 * <p>定义客户端与服务端之间的文件传输数据包结构，包含文件名、类型、偏移量、数据和压缩方式，
 * 支持断点续传和分块传输。</p>
 */
@Data
@AllArgsConstructor
public class TransferProtocol {

    private int fileNameLength;
    private String fileName;
    private ProtocolType type;
    private long offset;
    private byte[] data;
    private CompressionType compression;
}
