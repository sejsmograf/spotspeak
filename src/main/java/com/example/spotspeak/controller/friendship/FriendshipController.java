package com.example.spotspeak.controller.friendship;

import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public ResponseEntity<List<FriendshipUserInfoDTO>> getFriendsList(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<FriendshipUserInfoDTO> friends = friendshipService.getFriendshipDTOList(userId);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> deleteFriend(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID friendId) {
        String userId = jwt.getSubject();
        friendshipService.deleteFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
