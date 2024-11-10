package com.example.spotspeak.dto;

public record LocationConditionDTO(
    String type,
    Double radiusInMeters,
    Double longitude,
    Double latitude
) {
}
