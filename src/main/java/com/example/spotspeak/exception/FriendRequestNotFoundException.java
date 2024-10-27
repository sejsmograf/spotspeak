package com.example.spotspeak.exception;

public class FriendRequestNotFoundException extends RuntimeException{
    public FriendRequestNotFoundException(String message) {
        super(message);
    }
}
