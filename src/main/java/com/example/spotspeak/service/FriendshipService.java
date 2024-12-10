package com.example.spotspeak.service;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.entity.enumeration.ERelationStatus;
import com.example.spotspeak.exception.FriendshipNotFoundException;
import com.example.spotspeak.mapper.FriendshipMapper;
import com.example.spotspeak.mapper.UserMapper;
import com.example.spotspeak.repository.FriendRequestRepository;
import com.example.spotspeak.service.achievement.AchievementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {
    private FriendRequestRepository friendRequestRepository;
    private UserService userService;
    private FriendshipMapper friendshipMapper;
    private UserMapper userMapper;
    private AchievementService achievementService;

    public FriendshipService(UserService userService,
                             FriendshipMapper friendshipMapper,
                             UserMapper userMapper,
                             FriendRequestRepository friendRequestRepository,
                             AchievementService achievementService) {
        this.userService = userService;
        this.friendshipMapper = friendshipMapper;
        this.userMapper = userMapper;
        this.friendRequestRepository = friendRequestRepository;
        this.achievementService = achievementService;
    }

    public List<FriendshipUserInfoDTO> getFriendshipDTOList(String userId) {
        User currentUser = userService.findByIdOrThrow(userId);

        List<FriendRequest> friendships = friendRequestRepository.findAllAcceptedByUser(currentUser, EFriendRequestStatus.ACCEPTED);

        return friendships.stream()
                .map(friendship -> {
                    User friend = friendship.getSender().equals(currentUser)
                            ? friendship.getReceiver()
                            : friendship.getSender();
                    Integer totalPoints = achievementService.getTotalPointsByUser(friend);
                    return friendshipMapper.toFriendshipUserInfoDTO(friendship, friend, totalPoints);
                })
                .toList();
    }

    public List<User> getFriends(String userId) {
        User currentUser = userService.findByIdOrThrow(userId);

        List<FriendRequest> friendships = friendRequestRepository.findAllAcceptedByUser(currentUser, EFriendRequestStatus.ACCEPTED);

        return friendships.stream()
            .map(friendship -> friendship.getSender().equals(currentUser)
                ? friendship.getReceiver()
                : friendship.getSender())
            .toList();
    }

    public List<User> getMutualFriends(String currentUserId, String otherUserId) {
        List<User> currentUserFriends = getFriends(currentUserId);

        List<User> otherUserFriends = getFriends(otherUserId);

        return currentUserFriends.stream()
                .filter(otherUserFriends::contains)
                .toList();
    }

    public List<AuthenticatedUserProfileDTO> getMutualFriendsDTO(String currentUserId, String otherUserId) {
        List<User> mutualFriends = getMutualFriends(currentUserId, otherUserId);

        return mutualFriends.stream()
                .map(friend -> {
                    Integer totalPoints = achievementService.getTotalPointsByUser(friend);
                    return userMapper.createAuthenticatedUserProfileDTO(friend, totalPoints);
                })
                .toList();
    }

    public ERelationStatus getFriendshipStatus(String currentUserId, String userId) {
        User currentUser = userService.findByIdOrThrow(currentUserId);
        User otherUser = userService.findByIdOrThrow(userId);

        boolean isFriend = friendRequestRepository.existsAcceptedByUsers(currentUser, otherUser, EFriendRequestStatus.ACCEPTED);

        boolean invitationSent = friendRequestRepository.existsBySenderAndReceiverAndStatus(
                currentUser, otherUser, EFriendRequestStatus.PENDING);

        boolean invitationReceived = friendRequestRepository.existsBySenderAndReceiverAndStatus(
                otherUser, currentUser, EFriendRequestStatus.PENDING);

        if (isFriend) {
            return ERelationStatus.FRIENDS;
        } else if (invitationSent) {
            return ERelationStatus.INVITATION_SENT;
        } else if (invitationReceived) {
            return ERelationStatus.INVITATION_RECEIVED;
        } else {
            return ERelationStatus.NO_RELATION;
        }
    }

    @Transactional
    public void deleteFriend(String userId, UUID friendId) {
        User user = userService.findByIdOrThrow(userId);
        User friend = userService.findByIdOrThrow(String.valueOf(friendId));
        FriendRequest friendship = friendRequestRepository.findAcceptedByUsers(user, friend, EFriendRequestStatus.ACCEPTED)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found between users"));
        friendRequestRepository.delete(friendship);
    }
}
