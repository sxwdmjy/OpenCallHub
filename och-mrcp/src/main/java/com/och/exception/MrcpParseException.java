package com.och.exception;

public class MrcpParseException extends Exception {
    public MrcpParseException(String message) {
        super(message);
    }

    public MrcpParseException(String msg, Exception e) {
        super(msg, e);
    }
}
