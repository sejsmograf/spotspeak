package com.example.spotspeak.dto;

import com.example.spotspeak.entity.enumeration.ETraceType;

import java.time.LocalDateTime;
import java.util.List;

public record TraceDownloadDTO(
        Long id,
        PublicUserProfileDTO author,
        String resourceAccessUrl,
        String description,
        List<CommentResponseDTO> comments,
        Double latitude,
        Double longitude,
        ETraceType type,
        LocalDateTime createdAt) {
}
