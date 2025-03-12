package com.och.file.server.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

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
