package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.achievement.AchievementResponseDTO;
import com.example.spotspeak.dto.achievement.ConditionDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.service.ResourceService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AchievementMapper {

    private final ResourceService resourceService;

    public AchievementMapper(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public AchievementResponseDTO createAchievementResponseDTO(Achievement achievement) {
        Resource resource = achievement.getIconUrl();
        String resourceUrl = resource != null
            ? resourceService.getResourceAccessUrl(resource.getId())
            : null;

        List<ConditionDTO> conditionDTOs = achievement.getConditions() != null
            ? achievement.getConditions().stream()
            .map(condition -> (ConditionDTO) condition.toDTO())
            .toList()
            : List.of();

        return new AchievementResponseDTO(
            achievement.getId(),
            achievement.getName(),
            achievement.getDescription(),
            resourceUrl,
            achievement.getPoints(),
            achievement.getEventType().name(),
            achievement.getRequiredQuantity(),
            conditionDTOs
        );
    }
}
