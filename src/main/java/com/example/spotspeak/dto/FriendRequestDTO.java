package com.example.spotspeak.dto;

import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendRequestDTO(
        Long id,
        UUID senderId,
        UUID receiverId,
        EFriendRequestStatus status,
        LocalDateTime sentAt,
        LocalDateTime acceptedAt,
        LocalDateTime rejectedAt
) {
}
