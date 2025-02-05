package com.och.mrcp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class MrcpResponse extends MrcpMessage {
    private int statusCode;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(version).append(" ").append(statusCode).append(" ").append("\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("\r\n").append(body);
        return sb.toString();
    }
}