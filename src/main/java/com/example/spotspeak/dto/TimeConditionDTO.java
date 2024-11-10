package com.example.spotspeak.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record TimeConditionDTO(
    String type,
    LocalDateTime requiredDateTime,
    LocalTime startTime,
    LocalTime endTime
) {
}

