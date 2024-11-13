package com.example.spotspeak.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisteredUserDTO(
        @NotNull UUID id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String email,
        @NotBlank String username,
        @NotNull LocalDateTime registeredAt) {
}
