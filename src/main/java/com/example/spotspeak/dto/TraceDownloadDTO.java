package com.example.spotspeak.dto;

import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TraceDownloadDTO(
        Long id,
        UUID authorId,
        String resourceAccessUrl,
        String description,
        List<Comment> comments,
        List<Tag> traceTags,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt) {
}
