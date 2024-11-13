package com.example.spotspeak.dto.achievement;

import java.time.LocalDateTime;

public record UserAchievementDTO(
    Long userAchievementId,
    String achievementName,
    String resourceAccessUrl,
    LocalDateTime completedAt
) {
}
