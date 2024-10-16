package com.example.spotspeak.exception;

public class KeycloakClientException extends RuntimeException {
    public int statusCode;

    public KeycloakClientException(String message, int statusCode) {
        super("Keycloak client has thrown an exception: " + statusCode + " " + message);
        this.statusCode = statusCode;
    }
}
