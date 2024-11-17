package com.example.spotspeak.dto;

import java.util.UUID;

public record CommentMentionDTO(
    UUID mentionedUserId,
    String username
) {
}
