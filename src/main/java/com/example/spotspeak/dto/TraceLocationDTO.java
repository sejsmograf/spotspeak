package com.example.spotspeak.dto;

import java.time.LocalDateTime;

import com.example.spotspeak.entity.enumeration.ETraceType;

import jakarta.validation.constraints.NotNull;

public record TraceLocationDTO(
        @NotNull Long id,
        @NotNull Double longitude,
        @NotNull Double latitude,
        @NotNull ETraceType type,
        boolean hasDiscovered,
        LocalDateTime createdAt) {
}
