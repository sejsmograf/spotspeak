package com.example.spotspeak.exception;

public class UnauthorizedFriendRequestAccessException extends RuntimeException {
    public UnauthorizedFriendRequestAccessException(String message) {
        super(message);
    }
}
