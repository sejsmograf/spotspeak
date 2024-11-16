package com.example.spotspeak.dto;

public record OtherUserProfileDTO(
    AuthenticatedUserProfileDTO userProfile,
    Integer totalPoints,
    String friendshipStatus
) {}
