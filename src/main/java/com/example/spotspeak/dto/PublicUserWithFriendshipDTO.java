package com.example.spotspeak.dto;

import java.util.UUID;

public record PublicUserWithFriendshipDTO(
        UUID id,
        String username,
        String profilePictureUrl,
        Boolean isFriend) {
}
