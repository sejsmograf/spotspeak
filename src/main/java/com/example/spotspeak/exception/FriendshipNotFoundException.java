package com.example.spotspeak.exception;

public class FriendshipNotFoundException extends RuntimeException{
    public FriendshipNotFoundException(String message) {
        super(message);
    }
}
