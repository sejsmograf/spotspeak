package com.example.spotspeak.dto;


import jakarta.validation.constraints.NotNull;

public record EventLocationDTO(
        @NotNull Long id,
        @NotNull Double longitude,
        @NotNull Double latitude,
        String name, 
        boolean isActive) {
}
