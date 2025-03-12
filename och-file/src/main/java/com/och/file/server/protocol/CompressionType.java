package com.och.file.server.protocol;

import lombok.Getter;

@Getter
public enum CompressionType {
    NONE(0),
    GZIP(1),
    SNAPPY(2);

    private final int value;

    CompressionType(int value) {
        this.value = value;
    }

    public static CompressionType valueOf(int value) {
        for (CompressionType compressionType : values()) {
            if (compressionType.value == value) {
                return compressionType;
            }
        }
        throw new IllegalArgumentException("Unknown compression type: " + value);
    }
}
