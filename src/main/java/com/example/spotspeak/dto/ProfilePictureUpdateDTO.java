package com.example.spotspeak.dto;

import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.constants.FileUploadConsants;
import com.example.spotspeak.validation.ValidFile;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;

@Schema(description = "DTO for updating user profile picture")
public record ProfilePictureUpdateDTO(
        @Parameter(description = "Profile picture file", required = true) @Schema(description = "Profile picture file", requiredMode = RequiredMode.REQUIRED) @Valid @ValidFile(maxSize = FileUploadConsants.PROFILE_PICTURE_MAX_SIZE, allowedTypes = {
                "image/jpeg", "image/jpg",
                "image/png" }) MultipartFile file){
}
