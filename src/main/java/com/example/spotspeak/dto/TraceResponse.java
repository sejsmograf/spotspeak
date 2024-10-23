package com.example.spotspeak.dto;

import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;

public record TraceResponse(
        Long id,
        String description,
        Double latitude,
        Double longitude,
        List<Comment> comments,
        List<Tag> traceTags,
//        User author,
        LocalDateTime createdAt,
        Boolean isActive
) {

}
