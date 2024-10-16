package com.example.spotspeak.exception;

public class KeycloakException extends RuntimeException {

    public KeycloakException(String message) {
        super("Keycloak has thrown an exception: " + message);
    }
}
