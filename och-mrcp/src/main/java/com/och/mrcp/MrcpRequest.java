package com.och.mrcp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class MrcpRequest extends MrcpMessage {
    private String method;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(version).append(" ").append(method).append(" ").append(requestId).append("\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        if (body != null && !body.isEmpty()) {
            sb.append("\r\n").append(body);
        }
        return sb.toString();
    }
}
