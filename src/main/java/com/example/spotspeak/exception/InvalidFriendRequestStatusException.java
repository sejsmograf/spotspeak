package com.example.spotspeak.exception;

public class InvalidFriendRequestStatusException extends RuntimeException {
    public InvalidFriendRequestStatusException(String message) {
        super(message);
    }
}