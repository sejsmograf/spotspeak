package com.example.spotspeak.exception;

public class KeycloakServerException extends RuntimeException {
    public int statusCode;

    public KeycloakServerException(String message, int statusCode) {
        super("Keycloak server has thrown an exception: " + statusCode + " " + message);
        this.statusCode = statusCode;
    }
}
