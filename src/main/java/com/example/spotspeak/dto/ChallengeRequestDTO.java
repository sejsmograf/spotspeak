package com.example.spotspeak.dto;

import jakarta.validation.constraints.NotBlank;

public record ChallengeRequestDTO(
        @NotBlank String password) {
}
