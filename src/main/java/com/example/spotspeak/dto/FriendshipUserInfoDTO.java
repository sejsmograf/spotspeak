package com.example.spotspeak.dto;

import java.time.LocalDateTime;

public record FriendshipUserInfoDTO(
        Long id,
        CurrentUserInfoDTO friendInfo,
        LocalDateTime createdAt
) {
}
