package com.example.spotspeak.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
                LocalDateTime timestamp,
                String message,
                String details) {

        public ErrorResponse(String message) {
                this(LocalDateTime.now(), message, null);
        };

        public ErrorResponse(String message, String details) {
                this(LocalDateTime.now(), message, details);
        };
}
