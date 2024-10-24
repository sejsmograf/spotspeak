package com.example.spotspeak.dto;

import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.Tag;

import java.util.List;

public record TraceDownloadDTO(
        Long id,
        String resourceAccessUrl,
        String description,
        List<Comment> comments,
        List<Tag> traceTags,
        Double latitude,
        Double longitude
// User user
) {
}
