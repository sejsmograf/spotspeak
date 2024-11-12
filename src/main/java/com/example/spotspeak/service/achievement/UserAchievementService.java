package com.example.spotspeak.service.achievement;

import com.example.spotspeak.dto.UserAchievementDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievements.Achievement;
import com.example.spotspeak.entity.achievements.UserAchievement;
import com.example.spotspeak.mapper.UserAchievementMapper;
import com.example.spotspeak.repository.UserAchievementRepository;
import com.example.spotspeak.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

}
