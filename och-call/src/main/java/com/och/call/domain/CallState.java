package com.och.call.domain;

public enum CallState {

    NEW("新呼叫"),
    RINGING("振铃中"),
    IVR("IVR处理中"),
    QUEUEING("排队中"),
    ASSIGNED("已分配"),
    BRIDGED("通话中"),
    VOICEMAIL("语音信箱"),
    HOLD("保持"),
    AFTER_CALL("后处理"),
    ENDED("已结束"),
    FAILED("失败");

    private final String label;

    CallState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return this != ENDED && this != FAILED;
    }

    public boolean isTerminal() {
        return this == ENDED || this == FAILED;
    }
}
