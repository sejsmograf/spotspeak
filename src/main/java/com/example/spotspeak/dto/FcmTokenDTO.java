package com.example.spotspeak.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenDTO(
        String fcmToken) {
}
