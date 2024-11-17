package com.example.spotspeak.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record CommentRequestDTO(
        @NotBlank String content,
        List<UUID> mentions
) {
}
