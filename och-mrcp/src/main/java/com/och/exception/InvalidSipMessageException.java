package com.och.exception;

public class InvalidSipMessageException extends RuntimeException{

    public InvalidSipMessageException(String message) {
        super(message);
    }
}
