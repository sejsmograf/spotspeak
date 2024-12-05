package com.example.spotspeak.service;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.Friendship;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.entity.enumeration.ERelationStatus;
import com.example.spotspeak.exception.FriendshipNotFoundException;
import com.example.spotspeak.mapper.FriendshipMapper;
import com.example.spotspeak.mapper.UserMapper;
import com.example.spotspeak.repository.FriendRequestRepository;
import com.example.spotspeak.repository.FriendshipRepository;
import com.example.spotspeak.service.achievement.AchievementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {
    private FriendshipRepository friendshipRepository;
    private FriendRequestRepository friendRequestRepository;
    private UserService userService;
    private FriendshipMapper friendshipMapper;
    private UserMapper userMapper;
    private AchievementService achievementService;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserService userService,
                             FriendshipMapper friendshipMapper,
                             UserMapper userMapper,
                             FriendRequestRepository friendRequestRepository,
                             AchievementService achievementService) {
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
        this.friendshipMapper = friendshipMapper;
        this.userMapper = userMapper;
        this.friendRequestRepository = friendRequestRepository;
        this.achievementService = achievementService;
    }

    @Transactional
    public Friendship createFriendship(User userInitiating, User userReceiving) {
        if (checkFriendshipExists(userInitiating, userReceiving)) {
            throw new FriendshipNotFoundException("Friendship between users already exists");
        }

        Friendship friendship = Friendship.builder()
                .userInitiating(userInitiating)
                .userReceiving(userReceiving)
                .build();

        return friendshipRepository.save(friendship);
    }

    public List<FriendshipUserInfoDTO> getFriendshipDTOList(String userId) {
        User currentUser = userService.findByIdOrThrow(userId);

        List<Friendship> friendships = friendshipRepository.findAllByUser(currentUser);

        return friendships.stream()
                .map(friendship -> {
                    User friend = friendship.getUserInitiating().equals(currentUser)
                            ? friendship.getUserReceiving()
                            : friendship.getUserInitiating();
                    Integer totalPoints = achievementService.getTotalPointsByUser(friend);
                    return friendshipMapper.toFriendshipUserInfoDTO(friendship, friend, totalPoints);
                })
                .toList();
    }

    public List<User> getFriends(String userId) {
        User currentUser = userService.findByIdOrThrow(userId);

        List<Friendship> friendships = friendshipRepository.findAllByUser(currentUser);

        return friendships.stream()
            .map(friendship -> friendship.getUserInitiating().equals(currentUser)
                ? friendship.getUserReceiving()
                : friendship.getUserInitiating())
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
                .map(friend -> userMapper.createAuthenticatedUserProfileDTO(friend,null))
                .toList();
    }

    public ERelationStatus getFriendshipStatus(String currentUserId, String userId) {
        User currentUser = userService.findByIdOrThrow(currentUserId);
        User otherUser = userService.findByIdOrThrow(userId);

        boolean isFriend = checkFriendshipExists(currentUser, otherUser);

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
        Friendship friendship = friendshipRepository.findByUsers(user, friend)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found between users"));
        friendshipRepository.delete(friendship);
    }

    public boolean checkFriendshipExists(User user1, User user2) {
        if (user1.equals(user2))
            return true;
        return friendshipRepository.findByUsers(user1, user2).isPresent();
    }
}
