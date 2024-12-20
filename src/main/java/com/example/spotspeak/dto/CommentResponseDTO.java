package com.example.spotspeak.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponseDTO(
        Long commentId,
        Long traceId,
        PublicUserProfileDTO author,
        String content,
        LocalDateTime createdAt,
        List<CommentMentionDTO> mentions
) {
}
