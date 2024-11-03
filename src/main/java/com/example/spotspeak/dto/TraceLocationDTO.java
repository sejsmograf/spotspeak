package com.example.spotspeak.dto;

import jakarta.validation.constraints.NotNull;

public record TraceLocationDTO(
        @NotNull Long id,
        @NotNull Double longitude,
        @NotNull Double latitude,
        boolean hasDisovered) {
}
