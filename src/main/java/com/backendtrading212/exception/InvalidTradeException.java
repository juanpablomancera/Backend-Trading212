package com.backendtrading212.exception;

public class InvalidTradeException extends RuntimeException {
    public InvalidTradeException(String msg) {
        super(msg);
    }
}
