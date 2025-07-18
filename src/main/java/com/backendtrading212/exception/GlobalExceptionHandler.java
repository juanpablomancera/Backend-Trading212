package com.backendtrading212.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleBalance(InsufficientBalanceException ex) {
        return ResponseEntity.badRequest().body("Insufficient balance: " + ex.getMessage());
    }

    @ExceptionHandler(InsufficientHoldingsException.class)
    public ResponseEntity<String> handleHoldings(InsufficientHoldingsException ex) {
        return ResponseEntity.badRequest().body("Insufficient holdings: " + ex.getMessage());
    }

    @ExceptionHandler(InvalidTradeException.class)
    public ResponseEntity<String> handleInvalid(InvalidTradeException ex) {
        return ResponseEntity.badRequest().body("Invalid trade: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        return ResponseEntity.internalServerError().body("Unexpected error: " + ex.getMessage());
    }
}