package com.example.spotspeak.dto;

import java.util.UUID;

public record AuthenticatedUserProfileDTO(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl,
        Integer totalPoints,
        Boolean receiveNotifications) {
}
