package com.example.spotspeak.dto;

import java.time.LocalDateTime;

public record FriendshipUserInfoDTO(
        Long id,
        AuthenticatedUserProfileDTO friendInfo,
        LocalDateTime createdAt) {
}
