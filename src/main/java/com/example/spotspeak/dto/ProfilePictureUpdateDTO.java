package com.example.spotspeak.dto;

import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.validation.ValidFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(description = "DTO for updating user profile picture")
public record ProfilePictureUpdateDTO(
                @Valid @ValidFile(maxSize = 1024 * 1024 * 5, allowedTypes = {
                                "image/jpeg",
                                "image/jpg", "image/png" }) MultipartFile file){
}
