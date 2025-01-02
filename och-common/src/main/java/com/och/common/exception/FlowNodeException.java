package com.och.common.exception;

public class FlowNodeException extends RuntimeException {
    public FlowNodeException(String message) {
        super(message);
    }

    public FlowNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowNodeException(Throwable cause) {
        super(cause);
    }
}
