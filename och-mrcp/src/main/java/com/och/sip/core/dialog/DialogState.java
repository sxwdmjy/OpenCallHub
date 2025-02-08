package com.och.sip.core.dialog;

public enum DialogState {
    EARLY,       // Dialog 已创建但未确认（如收到 200 OK 但未收到 ACK）
    CONFIRMED,   // Dialog 已确认（收到 ACK）
    TERMINATED   // Dialog 终止（如收到 BYE）
}
