package com.example.spotspeak.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record UserAchievementDTO(
    Long userAchievementId,
    String achievementName,
    String achievementDescription,
    String resourceAccessUrl,
    Integer points,
    Integer requiredQuantity,
    Integer quantityProgress,
    Integer currentStreak,
    LocalDate lastActionDate,
    LocalDateTime completedAt,
    Set<Object> conditions
) {}
