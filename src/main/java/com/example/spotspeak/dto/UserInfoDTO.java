package com.example.spotspeak.dto;

import java.util.UUID;

public record UserInfoDTO(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String profilePictureUrl) {
}
