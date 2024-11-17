package com.example.spotspeak.controller.user;

import java.util.List;
import java.util.UUID;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.OtherUserProfileDTO;
import com.example.spotspeak.service.FriendshipService;
import com.example.spotspeak.service.achievement.UserAchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.RegisteredUserDTO;
import com.example.spotspeak.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private UserService userService;
    private UserAchievementService userAchievementService;
    private FriendshipService friendshipService;

    public UserController(UserService userService,
                          UserAchievementService userAchievementService,
                          FriendshipService friendshipService) {
        this.userService = userService;
        this.userAchievementService = userAchievementService;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PublicUserProfileDTO>> searchUsersByUsername(
            @Valid @NotBlank @RequestParam String username) {
        List<PublicUserProfileDTO> users = userService.searchUsersByUsername(username);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/init")
    @PreAuthorize("hasRole('INITIALIZE_ACCOUNT')")
    public ResponseEntity<Void> initializeKeycloakUser(
            @Valid @RequestBody RegisteredUserDTO userDTO) {
        userService.initializeUser(userDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/{userId}")
    ResponseEntity<OtherUserProfileDTO> getUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId) {
        String currentUserId = jwt.getSubject();
        String otherUserId = String.valueOf(userId);
        AuthenticatedUserProfileDTO userInfo = userService.getUserInfo(otherUserId);
        Integer totalPoints = userAchievementService.getTotalPointsByUser(otherUserId);
        String friendshipStatus = friendshipService.getFriendshipStatus(currentUserId, otherUserId);
        OtherUserProfileDTO otherUserProfileDTO = userService.getOtherUserInfo(userInfo, totalPoints, friendshipStatus);
        return ResponseEntity.ok(otherUserProfileDTO);
    }
}
