package com.example.spotspeak.dto;

public record ErrorResponse(
        int httpStatusCode,
        String message) {
}
