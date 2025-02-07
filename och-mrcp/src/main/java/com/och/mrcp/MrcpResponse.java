package com.och.mrcp;

import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class MrcpResponse extends MrcpMessage {


    private int statusCode;

    private String statusText;

    private int messageLength = -1;

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(getVersion());
        sb.append(' ').append(getMessageLength());
        sb.append(' ').append(getRequestId());
        sb.append(' ').append(getStatusCode());
        sb.append(' ').append(getStatusText());
        sb.append(CRLF);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(CRLF);
        }
        sb.append(CRLF);
        if (!StringUtil.isNullOrEmpty(body)) {
            sb.append(body);
        }
        return sb.toString();
    }
}
