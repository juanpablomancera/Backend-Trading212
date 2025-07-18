package com.backendtrading212.exception;

public class InsufficientHoldingsException extends RuntimeException {
    public InsufficientHoldingsException(String msg) {
        super(msg);
    }
}