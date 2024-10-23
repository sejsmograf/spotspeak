package com.example.spotspeak.exception;

import lombok.Getter;

@Getter
public class KeycloakClientException extends RuntimeException {
    private String details;

    public KeycloakClientException(String message) {
        super(message);
    }

    public KeycloakClientException(String message, Integer statusCode) {
        super(message);
    }

    public KeycloakClientException(String message, String details) {
        super(message);
        this.details = details;
    }

}
