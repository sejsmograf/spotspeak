package com.example.spotspeak.dto;

import com.example.spotspeak.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record TraceResponse(
        Long id,
        String description,
        Double latitude,
        Double longitude,
        List<Comment> comments,
        LocalDateTime createdAt,
        Boolean isActive
) {

}
