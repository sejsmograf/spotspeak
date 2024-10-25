package com.example.spotspeak.dto;

import java.util.UUID;

public record CurrentUserInfoDTO(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl) {
}
