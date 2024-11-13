package com.example.spotspeak.dto.achievement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AchievementUpdateDTO(
    @NotNull Long id,
    @NotBlank String name,
    @NotBlank String description,
    @NotNull Integer points,
    @NotBlank String eventType,
    @NotNull Integer requiredQuantity,
    List<ConditionDTO> conditions
) {}