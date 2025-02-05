package com.och.sip.core.message;

import io.netty.util.Recycler;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SIP响应消息 (RFC 3261 7.2节)
 */
public class SipResponse extends SipMessage{

    private int statusCode;
    private String reasonPhrase;

    private Recycler.Handle<SipResponse> handle;

    public SipResponse(Recycler.Handle<SipResponse> handle) {
        this.handle = handle;
        this.messageType = Type.RESPONSE;
    }

    public SipResponse(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }



    public SipResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public SipResponse setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
        return this;
    }



    @Override
    public String getStartLine() {
        return "SIP/2.0 " + statusCode + " " + reasonPhrase;
    }

    @Override
    public String getCallId() {
        return getHeader("Call-ID");
    }

    @Override
    public String getFromTag() {
        String fromHeader = getHeader("From");
        return parseTagParameter(fromHeader);
    }


    public void recycle() {
        reset();
        statusCode = 0;
        reasonPhrase = null;
        handle.recycle(this);
    }

    // 判断是否为最终响应（RFC 3261 21.1）
    public boolean isFinal() {
        return statusCode >= 200 && statusCode < 700;
    }

    // 新增方法：解析To头中的tag参数
    public String getToTag() {
        String toHeader = getHeader("To");
        if (toHeader != null) {
            return parseTagParameter(toHeader);
        }
        return null;
    }

    @Override
    public String getMethod() {
        return "";
    }

    @Override
    public String getCSeq() {
        return getHeader("CSeq");
    }

    // 复用SipRequest中的解析逻辑
    private String parseTagParameter(String headerValue) {
        Pattern pattern = Pattern.compile(";\\s*tag=([^;]+)");
        Matcher matcher = pattern.matcher(headerValue);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStartLine()).append("\r\n");
        for (String key : headers.keySet()) {
            sb.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }
        sb.append("\r\n");
        if (body != null) sb.append(body);
        return sb.toString();
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
