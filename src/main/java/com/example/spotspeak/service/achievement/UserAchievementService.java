package com.example.spotspeak.service.achievement;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDetailsDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.exception.UserAchievementNotFoundException;
import com.example.spotspeak.mapper.UserAchievementMapper;
import com.example.spotspeak.mapper.UserMapper;
import com.example.spotspeak.repository.UserAchievementRepository;
import com.example.spotspeak.service.FriendshipService;
import com.example.spotspeak.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserAchievementService {

    private UserAchievementRepository userAchievementRepository;
    private UserService userService;
    private UserAchievementMapper userAchievementMapper;
    private FriendshipService friendshipService;
    private UserMapper userMapper;
    private Logger logger = LoggerFactory.getLogger(AchievementService.class);

    public UserAchievementService(UserAchievementRepository userAchievementRepository, UserService userService, UserAchievementMapper userAchievementMapper, FriendshipService friendshipService, UserMapper userMapper) {
        this.userAchievementRepository = userAchievementRepository;
        this.userService = userService;
        this.userAchievementMapper = userAchievementMapper;
        this.friendshipService = friendshipService;
        this.userMapper = userMapper;
    }

    public List<UserAchievementDTO> getUserAchievements(String userId) {
        User user = userService.findByIdOrThrow(userId);

        return userAchievementRepository.findByUser(user).stream()
            .map(userAchievementMapper::toUserAchievementDTO)
            .toList();
    }

    public UserAchievementDetailsDTO getUserAchievementDetails(String userId, Long userAchievementId) {
        User user = userService.findByIdOrThrow(userId);

        UserAchievement userAchievement = userAchievementRepository.findByIdAndUser(userAchievementId, user)
            .orElseThrow(() -> new UserAchievementNotFoundException("User achievement not found"));

        return userAchievementMapper.toUserAchievementDetailsDTO(userAchievement);
    }

    public List<PublicUserProfileDTO> getFriendsWhoCompletedAchievement(String userId, Long userAchievementId) {
        User user = userService.findByIdOrThrow(userId);

        UserAchievement userAchievement = userAchievementRepository.findByIdAndUser(userAchievementId, user)
            .orElseThrow(() -> new UserAchievementNotFoundException("User achievement not found"));

        List<User> friends = friendshipService.getFriendsList(userId).stream()
            .map(FriendshipUserInfoDTO::friendInfo)
            .map(AuthenticatedUserProfileDTO::id)
            .map(friendId -> {
                try {
                    return userService.findByIdOrThrow(friendId.toString());
                } catch (Exception e) {
                    logger.warn("Failed to find user with ID who completed achievement: " + friendId, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();

        return friends.stream()
            .filter(friend -> userAchievementRepository
                .findCompletedByAchievementAndUser(userAchievement.getAchievement(), friend)
                .isPresent()
            )
            .map(userMapper::createPublicUserProfileDTO)
            .toList();
    }


    public List<UserAchievementDTO> getCompletedAchievementsByUser(String currentUserId, UUID userId) {
        userService.findByIdOrThrow(currentUserId);
        User user = userService.findByIdOrThrow(String.valueOf(userId));
        return userAchievementRepository.findCompletedAchievementsByUser(user).stream()
            .map(userAchievementMapper::toUserAchievementDTO)
            .toList();
    }
}
