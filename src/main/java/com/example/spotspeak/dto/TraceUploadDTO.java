package com.example.spotspeak.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record TraceUploadDTO(
        @NotNull Double longitude,
        @NotNull Double latitude,
        String description,
        List<Long> tagIds) {
}
