package com.example.spotspeak.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(
        @NotBlank String passwordChallengeToken,
        String firstName,
        String lastName,
        String email,
        String username) {
}
