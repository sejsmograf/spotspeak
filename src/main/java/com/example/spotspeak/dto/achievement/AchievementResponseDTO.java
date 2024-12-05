package com.example.spotspeak.dto.achievement;

import java.util.List;

public record AchievementResponseDTO(
    Long id,
    String name,
    String description,
    String resourceAccessUrl,
    Integer points,
    String eventType,
    Integer requiredQuantity,
    List<ConditionDTO> conditions
) {
}

