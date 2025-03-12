package com.och.file.server.protocol;

import lombok.Getter;

@Getter
public enum ProtocolType {

    HANDSHAKE(0x01), // 握手
    FILE_TRANSFER(0x02),
    ACK(0x03),// 确认
    ERROR(0x04),// 错误
    FILE_END(0x05),// 文件传输完成
    ;
    private final int value;

    ProtocolType(int value) {
        this.value = value;
    }

    public static ProtocolType valueOf(int value) {
        for (ProtocolType protocolType : ProtocolType.values()) {
            if (protocolType.value == value) {
                return protocolType;
            }
        }
        return null;
    }
}
