package com.example.spotspeak.dto.achievement;

import java.time.LocalDateTime;

public record UserAchievementDetailsDTO(
    Long userAchievementId,
    String achievementName,
    String achievementDescription,
    String resourceAccessUrl,
    Integer points,
    Integer requiredQuantity,
    Integer quantityProgress,
    Integer currentStreak,
    LocalDateTime completedAt,
    LocalDateTime endTime
) {}
