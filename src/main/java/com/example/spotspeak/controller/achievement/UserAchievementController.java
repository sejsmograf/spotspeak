package com.example.spotspeak.controller.achievement;

import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.service.achievement.UserAchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/achievements")
public class UserAchievementController {

    private final UserAchievementService userAchievementService;

    public UserAchievementController(UserAchievementService userAchievementService) {
        this.userAchievementService = userAchievementService;
    }

    @GetMapping
    public ResponseEntity<List<UserAchievementDTO>> getUserAchievements(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<UserAchievementDTO> userAchievements = userAchievementService.getUserAchievements(userId);
        return ResponseEntity.ok(userAchievements);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserAchievementDTO>> getCompletedAchievementsByUser(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        String currentUserId = jwt.getSubject();
        List<UserAchievementDTO> userAchievements = userAchievementService.getCompletedAchievementsByUser(currentUserId, userId);
        return ResponseEntity.ok(userAchievements);
    }
}
