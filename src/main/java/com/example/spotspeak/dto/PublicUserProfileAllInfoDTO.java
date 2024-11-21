package com.example.spotspeak.dto;

import com.example.spotspeak.entity.enumeration.ERelationStatus;

public record PublicUserProfileAllInfoDTO(
    AuthenticatedUserProfileDTO userProfile,
    ERelationStatus relationshipStatus
) {}
