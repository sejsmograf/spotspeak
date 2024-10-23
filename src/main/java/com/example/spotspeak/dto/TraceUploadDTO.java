package com.example.spotspeak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TraceUploadDTO(
                @NotNull Double latitude,
                @NotNull Double longitude,
                @NotBlank String description) {
}
