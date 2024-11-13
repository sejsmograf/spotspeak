package com.example.spotspeak.controller.admin;

import com.example.spotspeak.constants.FileUploadConsants;
import com.example.spotspeak.dto.achievement.AchievementResponseDTO;
import com.example.spotspeak.dto.achievement.AchievementUpdateDTO;
import com.example.spotspeak.dto.achievement.AchievementUploadDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.mapper.AchievementMapper;
import com.example.spotspeak.service.UserService;
import com.example.spotspeak.service.achievement.AchievementService;
import com.example.spotspeak.validation.ValidFile;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private AchievementService achievementService;
    private UserService userService;
    private AchievementMapper mapper;

    public AdminController(AchievementService achievementService, UserService userService, AchievementMapper mapper) {
        this.achievementService = achievementService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementResponseDTO>> getAchievements(@AuthenticationPrincipal Jwt jwt) {
        List<Achievement> achievements = achievementService.getAllAchievements();
        List<AchievementResponseDTO> achievementResponseDTOs = achievements.isEmpty()
                ? List.of()
                : achievements.stream()
                        .map(mapper::createAchievementResponseDTO)
                        .toList();
        return ResponseEntity.ok(achievementResponseDTOs);
    }

    @PostMapping("/create-achievement")
    public ResponseEntity<AchievementResponseDTO> createAchievement(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ValidFile(required = false, maxSize = FileUploadConsants.ACHIEVEMENT_ICON_MAX_SIZE, allowedTypes = {
                    "image/jpeg", "image/png",
                    "image/jpg" }) @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart @Valid AchievementUploadDTO achievementUploadDTO) {
        Achievement achievement = achievementService.createAchievement(file, achievementUploadDTO);
        List<User> users = userService.getAllUsers();
        achievementService.initializeAchievementsForAllUsers(users);
        AchievementResponseDTO dto = mapper.createAchievementResponseDTO(achievement);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/update-achievement")
    public ResponseEntity<AchievementResponseDTO> updateAchievement(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart @Valid AchievementUpdateDTO achievementUpdateDTO) {
        Achievement updatedAchievement = achievementService.updateAchievement(file, achievementUpdateDTO);
        AchievementResponseDTO dto = mapper.createAchievementResponseDTO(updatedAchievement);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/delete-achievement/{id}")
    public ResponseEntity<Void> deleteAchievement(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.noContent().build();
    }
}
