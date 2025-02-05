package com.och.exception;

public class InvalidGrammarException extends RuntimeException {
    public InvalidGrammarException(String message) {
        super(message);
    }
}
