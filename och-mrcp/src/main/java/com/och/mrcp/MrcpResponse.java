package com.och.mrcp;

import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class MrcpResponse extends MrcpMessage {


    private int statusCode; // 状态码

    private String requestState;

    private int messageLength = -1;

    //方法名
    private String method;

    @Override
    public String toString() {
        if(!StringUtil.isNullOrEmpty(method)){
            return toRecogString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getVersion());
        sb.append(' ').append(getMessageLength());
        sb.append(' ').append(getRequestId());
        sb.append(' ').append(getStatusCode());
        sb.append(' ').append(getRequestState());
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

    public String toRecogString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getVersion());
        sb.append(' ').append(getMessageLength());
        sb.append(' ').append(getMethod());
        sb.append(' ').append(getRequestId());
        sb.append(' ').append(getRequestState());
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
