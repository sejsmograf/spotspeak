package com.example.spotspeak.controller.friendship;

import com.example.spotspeak.dto.FriendRequestDTO;
import com.example.spotspeak.dto.FriendRequestUserInfoDTO;
import com.example.spotspeak.service.FriendRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    FriendRequestService friendRequestService;

    public FriendRequestController(FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<FriendRequestDTO> sendFriendRequest(@AuthenticationPrincipal Jwt jwt,
                                                                      @PathVariable UUID receiverId) {
        String senderId = jwt.getSubject();
        FriendRequestDTO friendRequestDTO = friendRequestService.sendFriendRequest(senderId, receiverId);
        return ResponseEntity.ok(friendRequestDTO);
    }

    @PutMapping("/accept/{requestId}")
    public ResponseEntity<FriendRequestDTO> acceptFriendRequest(@AuthenticationPrincipal Jwt jwt,
                                                                @PathVariable Long requestId) {
        String currentUserId = jwt.getSubject();
        FriendRequestDTO friendRequestDTO = friendRequestService.acceptFriendRequest(currentUserId, requestId);
        return ResponseEntity.ok(friendRequestDTO);
    }

    @PutMapping("/reject/{requestId}")
    public ResponseEntity<FriendRequestDTO> rejectFriendRequest(@AuthenticationPrincipal Jwt jwt,
                                                                @PathVariable Long requestId) {
        String currentUserId = jwt.getSubject();
        FriendRequestDTO friendRequestDTO = friendRequestService.rejectFriendRequest(currentUserId, requestId);
        return ResponseEntity.ok(friendRequestDTO);
    }

    @DeleteMapping("/cancel/{requestId}")
    public ResponseEntity<Void> cancelFriendRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable Long requestId) {
        String currentUserId = jwt.getSubject();
        friendRequestService.cancelFriendRequest(currentUserId, requestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sent")
    public ResponseEntity<List<FriendRequestUserInfoDTO>> getSentFriendRequests(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<FriendRequestUserInfoDTO> sentRequests = friendRequestService.getSentFriendRequests(userId);
        return ResponseEntity.ok(sentRequests);
    }

    @GetMapping("/received")
    public ResponseEntity<List<FriendRequestUserInfoDTO>> getReceivedFriendRequests(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<FriendRequestUserInfoDTO> receivedRequests = friendRequestService.getReceivedFriendRequests(userId);
        return ResponseEntity.ok(receivedRequests);
    }
}
