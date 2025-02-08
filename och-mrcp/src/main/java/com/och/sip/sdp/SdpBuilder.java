package com.och.sip.sdp;

public class SdpBuilder {
    private final StringBuilder sb = new StringBuilder(512);

    public SdpBuilder append(String key, String value) {
        sb.append(key).append(value);
        return this;
    }

    public SdpBuilder append(String value){
        sb.append(value);
        return this;
    }

    public SdpBuilder append(int value){
        sb.append(value);
        return this;
    }

    public SdpBuilder space() {
        sb.append(' ');
        return this;
    }

    public SdpBuilder newLine() {
        sb.append("\r\n");
        return this;
    }

    public SdpBuilder appendOptional(String prefix, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(prefix).append(value).append("\r\n");
        }
        return this;
    }

    public String build() {
        return sb.toString();
    }
}
