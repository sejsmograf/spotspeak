package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDetailsDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.service.ResourceService;
import org.springframework.stereotype.Component;

@Component
public class UserAchievementMapper {
    private final ResourceService resourceService;

    public UserAchievementMapper(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public UserAchievementDetailsDTO toUserAchievementDetailsDTO(UserAchievement userAchievement) {
        Resource resource = userAchievement.getAchievement().getIconUrl();
        String resourceUrl = resource != null
            ? resourceService.getResourceAccessUrl(resource.getId())
            : null;

        return new UserAchievementDetailsDTO(
            userAchievement.getId(),
            userAchievement.getAchievement().getName(),
            userAchievement.getAchievement().getDescription(),
            resourceUrl,
            userAchievement.getAchievement().getPoints(),
            userAchievement.getAchievement().getRequiredQuantity(),
            userAchievement.getQuantityProgress(),
            userAchievement.getCurrentStreak(),
            userAchievement.getCompletedAt()
        );
    }

    public UserAchievementDTO toUserAchievementDTO(UserAchievement userAchievement) {
        Resource resource = userAchievement.getAchievement().getIconUrl();
        String resourceUrl = resource != null
            ? resourceService.getResourceAccessUrl(resource.getId())
            : null;

        return new UserAchievementDTO(
            userAchievement.getId(),
            userAchievement.getAchievement().getName(),
            resourceUrl,
            userAchievement.getAchievement().getPoints(),
            userAchievement.getAchievement().getEventType().toString(),
            userAchievement.getCompletedAt()
        );
    }
}
