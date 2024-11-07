package com.example.spotspeak.dto;

import java.time.Instant;
import java.util.UUID;

public record ChallengeResponseDTO(
        Instant issuedAt,
        UUID issuedFor,
        String token) {
}
