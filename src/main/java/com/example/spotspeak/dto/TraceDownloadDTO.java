package com.example.spotspeak.dto;

import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;

public record TraceDownloadDTO(
        Long id,
        PublicUserProfileDTO author,
        String resourceAccessUrl,
        String description,
        List<Comment> comments,
        List<Tag> traceTags,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt) {
}
