package com.och.sip.core.message;

import io.netty.util.Recycler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SIP请求消息 (RFC 3261 7.1节)
 */
public class SipRequest extends SipMessage {

    private String method;
    private String uri;
    private Recycler.Handle<SipRequest> handle;

    private static final Recycler<SipRequest> RECYCLER = new Recycler<>() {
        @Override
        protected SipRequest newObject(Handle<SipRequest> handle) {
            return new SipRequest(handle);
        }
    };

    public static SipRequest newInstance() {
        return RECYCLER.get();
    }

    public SipRequest(Recycler.Handle<SipRequest> handle) {
        this.handle = handle;
        this.messageType = Type.REQUEST;
    }

    public SipRequest(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public SipRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public SipRequest setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String getStartLine() {
        return method + " " + uri + " SIP/2.0";
    }

    public void recycle() {
        reset();
        method = null;
        uri = null;
        handle.recycle(this);
    }

    public String getBranchId() {
        String via = getHeader("Via");
        if (via != null) {
            Pattern pattern = Pattern.compile("branch=([^;]+)");
            Matcher matcher = pattern.matcher(via);
            return matcher.find() ? matcher.group(1) : null;
        }
        return null;
    }

    @Override
    public String getCallId() {
        return getHeader("Call-ID");
    }

    @Override
    public String getFromTag() {
        String fromHeader = getHeader("From");
        if (fromHeader != null) {
            return parseTagParameter(fromHeader);
        }
        return null;
    }

    @Override
    public String getToTag() {
        String toHeader = getHeader("To");
        return parseTagParameter(toHeader);
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getCSeq() {
        return getHeader("CSeq");
    }

    private String parseTagParameter(String headerValue) {
        Pattern pattern = Pattern.compile(";\\s*tag=([^;]+)");
        Matcher matcher = pattern.matcher(headerValue);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    // 判断是否为INVITE方法
    public boolean isInvite() {
        return "INVITE".equals(method);
    }

    // 判断是否为ACK方法
    public boolean isAck() {
        return "ACK".equals(method);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStartLine()).append("\r\n");
        for (String name : headers.keySet()) {
            sb.append(name).append(": ").append(headers.get(name)).append("\r\n");
        }
        sb.append("\r\n");
        sb.append(body);
        return sb.toString();
    }
}
