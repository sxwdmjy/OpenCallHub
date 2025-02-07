package com.och.rtp;

import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class RtpPacket {
    private final int version;
    private final boolean padding;
    private final boolean extension;
    private final int csrcCount;
    private final boolean marker;
    private final int payloadType;
    private final int sequenceNumber;
    private final long timestamp;
    private final long ssrc;
    private final byte[] payload;

    public RtpPacket(int version, boolean padding, boolean extension, int csrcCount, boolean marker,
                     int payloadType, int sequenceNumber, long timestamp, long ssrc, byte[] payload) {
        this.version = version;
        this.padding = padding;
        this.extension = extension;
        this.csrcCount = csrcCount;
        this.marker = marker;
        this.payloadType = payloadType;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.ssrc = ssrc;
        this.payload = payload;
    }

}
