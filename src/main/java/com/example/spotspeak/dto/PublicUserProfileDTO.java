package com.example.spotspeak.dto;

import java.util.UUID;

public record PublicUserProfileDTO(
        UUID id,
        String username,
        String profilePictureUrl) {
}
