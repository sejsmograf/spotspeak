package com.example.spotspeak.exception;

import jakarta.ws.rs.ForbiddenException;

public class PasswordChallengeFailedException extends RuntimeException {

    public PasswordChallengeFailedException(String message) {
        super(message);
    }
}
