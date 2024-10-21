package com.example.spotspeak.exception;

public class TraceNotFoundException extends RuntimeException {
    public TraceNotFoundException(String message) {
        super(message);
    }
}