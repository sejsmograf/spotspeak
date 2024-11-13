package com.example.spotspeak.exception;

public class AchievementNotFoundException extends RuntimeException {
    public AchievementNotFoundException(String message) {
        super(message);
    }
}
