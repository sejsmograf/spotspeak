package com.example.spotspeak.service.achievement;

import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.mapper.UserAchievementMapper;
import com.example.spotspeak.repository.UserAchievementRepository;
import com.example.spotspeak.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserAchievementService {

    private UserAchievementRepository userAchievementRepository;
    private UserService userService;
    private UserAchievementMapper userAchievementMapper;

    public UserAchievementService(UserAchievementRepository userAchievementRepository, UserService userService, UserAchievementMapper userAchievementMapper) {
        this.userAchievementRepository = userAchievementRepository;
        this.userService = userService;
        this.userAchievementMapper = userAchievementMapper;
    }

    public List<UserAchievementDTO> getUserAchievements(String userId) {
        User user = userService.findByIdOrThrow(userId);

        return userAchievementRepository.findByUser(user).stream()
            .map(userAchievementMapper::toUserAchievementDTO)
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
