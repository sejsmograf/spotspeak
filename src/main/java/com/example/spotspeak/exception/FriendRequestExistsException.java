package com.example.spotspeak.exception;

public class FriendRequestExistsException extends RuntimeException{
    public FriendRequestExistsException(String message) {
        super(message);
    }
}
