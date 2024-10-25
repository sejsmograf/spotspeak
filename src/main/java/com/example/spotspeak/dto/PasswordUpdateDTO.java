package com.example.spotspeak.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateDTO(
        @NotBlank String currentPassword,
        @NotBlank String newPassword) {
}
