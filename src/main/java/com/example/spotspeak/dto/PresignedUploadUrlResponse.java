package com.example.spotspeak.dto;

public record PresignedUploadUrlResponse(
        String presignedUrl,
        String keyName) {
}
