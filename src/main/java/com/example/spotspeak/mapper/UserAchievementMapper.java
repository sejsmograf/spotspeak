package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.UserAchievementDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.achievements.Condition;
import com.example.spotspeak.entity.achievements.UserAchievement;
import com.example.spotspeak.service.ResourceService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserAchievementMapper {
    private final ResourceService resourceService;

    public UserAchievementMapper(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public UserAchievementDTO toUserAchievementDTO(UserAchievement userAchievement) {
        Set<Object> conditionDTOs = userAchievement.getAchievement().getConditions() != null
            ? userAchievement.getAchievement().getConditions().stream()
            .map(Condition::toDTO)
            .collect(Collectors.toSet())
            : Collections.emptySet();

        Resource resource = userAchievement.getAchievement().getIconUrl();
        String resourceUrl = resource != null
            ? resourceService.getResourceAccessUrl(resource.getId())
            : null;

        return new UserAchievementDTO(
            userAchievement.getId(),
            userAchievement.getAchievement().getName(),
            userAchievement.getAchievement().getDescription(),
            resourceUrl,
            userAchievement.getAchievement().getPoints(),
            userAchievement.getAchievement().getRequiredQuantity(),
            userAchievement.getQuantityProgress(),
            userAchievement.getCurrentStreak(),
            userAchievement.getLastActionDate(),
            userAchievement.getCompletedAt(),
            conditionDTOs
        );
    }
}
