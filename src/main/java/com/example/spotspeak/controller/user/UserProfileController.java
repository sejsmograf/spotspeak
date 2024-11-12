package com.example.spotspeak.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.constants.FileUploadConsants;
import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.ChallengeRequestDTO;
import com.example.spotspeak.dto.ChallengeResponseDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.service.UserService;
import com.example.spotspeak.validation.ValidFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
@Validated
public class UserProfileController {

    private UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping
    ResponseEntity<AuthenticatedUserProfileDTO> updateProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        String userId = jwt.getSubject();
        userService.updateUser(userId, userUpdateDTO);
        AuthenticatedUserProfileDTO userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping
    ResponseEntity<AuthenticatedUserProfileDTO> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        AuthenticatedUserProfileDTO userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    @DeleteMapping
    ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate-challenge")
    ResponseEntity<ChallengeResponseDTO> generateChallenge(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChallengeRequestDTO challengeRequestDTO) {
        String userId = jwt.getSubject();
        boolean authenticatedWithGoogle = "google".equals(jwt.getClaim("identity_provider"));
        ChallengeResponseDTO response;

        if (authenticatedWithGoogle) {
            response = userService.generateTemporaryTokenForGoogleUser(userId);
        } else {
            response = userService.generateTemporaryToken(userId,
                    challengeRequestDTO.password());
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-password")
    ResponseEntity<Void> updatePassword(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        String userId = jwt.getSubject();
        userService.updateUserPassword(userId, passwordUpdateDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/picture")
    ResponseEntity<Resource> updateProfilePicture(@AuthenticationPrincipal Jwt jwt,
            @Valid @ValidFile(maxSize = FileUploadConsants.PROFILE_PICTURE_MAX_SIZE, allowedTypes = {
                    "image/jpeg", "image/jpg", "image/png"
            }) @RequestPart MultipartFile file) {
        String userId = jwt.getSubject();
        Resource resource = userService.updateUserProfilePicture(userId, file);
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/picture")
    ResponseEntity<Void> deleteProfilePicture(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        userService.deleteUserProfilePicture(userId);
        return ResponseEntity.noContent().build();
    }
}
