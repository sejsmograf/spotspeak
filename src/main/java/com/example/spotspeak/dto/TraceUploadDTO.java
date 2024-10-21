package com.example.spotspeak.dto;

public record TraceUploadDTO(
        Double latitude,
        Double longitude,
        String description,
        String keyName,
        String fileName,
        String fileType) {
}
