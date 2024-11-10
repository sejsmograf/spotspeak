package com.example.spotspeak.controller.achievement;

import com.example.spotspeak.dto.UserAchievementDTO;
import com.example.spotspeak.service.achievement.UserAchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/initialize")
    public ResponseEntity<Void> initializeAchievementsForUser(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        userAchievementService.initializeUserAchievements(userId);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/initialize-achievements")
//    public ResponseEntity<Void> initializeAchievementsForAllUsers() { //need admin authorization
//        List<User> allUsers = userService.getAllUsers();
//        allUsers.forEach(user -> userAchievementService.initializeUserAchievements(String.valueOf(user.getId())));
//        return ResponseEntity.noContent().build();
//    }
}
