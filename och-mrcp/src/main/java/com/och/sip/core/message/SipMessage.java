package com.och.sip.core.message;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SIP消息基类 (RFC 3261 7.1节)
 */
public abstract class SipMessage {
    public enum Type {REQUEST, RESPONSE}

    @Getter
    public Type messageType;
    public final Map<String, String> headers = new LinkedHashMap<>();
    public String body;

    public void reset() {
        headers.clear();
        body = null;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void addHeader(String name, String value) {
        headers.put(name.trim(), value.trim());
    }


    public boolean isRequest() {
        if (this.messageType == Type.REQUEST) {
            return true;
        }
        return false;
    }

    public abstract String getStartLine();

    public abstract String getCallId();

    public abstract String getFromTag();

    public abstract String getToTag();

    public abstract String getMethod();

    public abstract String getCSeq();
}
