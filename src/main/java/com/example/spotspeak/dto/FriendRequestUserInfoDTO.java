package com.example.spotspeak.dto;

import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;

import java.time.LocalDateTime;

public record FriendRequestUserInfoDTO(
        Long id,
        CurrentUserInfoDTO userInfo,
        EFriendRequestStatus status,
        LocalDateTime sentAt,
        LocalDateTime acceptedAt,
        LocalDateTime rejectedAt
) {
}
