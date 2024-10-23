package com.example.spotspeak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for updating user information. Not used for profile picture")
public record UserUpdateDTO(
                String firstName,
                String lastName) {
}
