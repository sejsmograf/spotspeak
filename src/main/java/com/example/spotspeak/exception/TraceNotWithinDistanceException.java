package com.example.spotspeak.exception;

public class TraceNotWithinDistanceException extends RuntimeException {
    public TraceNotWithinDistanceException(String message) {
        super(message);
    }
}
