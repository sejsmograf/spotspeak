package com.example.spotspeak.exception;

public class UserAchievementNotFoundException extends RuntimeException{
    public UserAchievementNotFoundException(String message) {
        super(message);
    }
}
