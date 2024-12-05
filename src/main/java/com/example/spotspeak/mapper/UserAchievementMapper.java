package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDetailsDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.TimeCondition;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import com.example.spotspeak.service.ResourceService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

        LocalDateTime endTime = calculateEndTime(userAchievement);

        return new UserAchievementDetailsDTO(
            userAchievement.getId(),
            userAchievement.getAchievement().getName(),
            userAchievement.getAchievement().getDescription(),
            resourceUrl,
            userAchievement.getAchievement().getPoints(),
            userAchievement.getAchievement().getRequiredQuantity(),
            userAchievement.getQuantityProgress(),
            userAchievement.getCurrentStreak(),
            userAchievement.getCompletedAt(),
            endTime
        );
    }

    public UserAchievementDTO toUserAchievementDTO(UserAchievement userAchievement) {
        Resource resource = userAchievement.getAchievement().getIconUrl();
        String resourceUrl = resource != null
            ? resourceService.getResourceAccessUrl(resource)
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

    private LocalDateTime calculateEndTime(UserAchievement userAchievement) {
        Achievement achievement = userAchievement.getAchievement();
        TimeCondition timeCondition = achievement.getConditions().stream()
            .filter(condition -> condition instanceof TimeCondition)
            .map(condition -> (TimeCondition) condition)
            .findFirst()
            .orElse(null);

        if (timeCondition == null) {
            return null;
        }

        LocalDateTime requiredDateTime = timeCondition.getRequiredDateTime();
        EDateGranularity granularity = timeCondition.getGranularity();

        if (granularity == EDateGranularity.ONLY_HOUR || granularity == EDateGranularity.TIME_RANGE) {
            return null;
        }

        return normalizeRequiredDateTime(requiredDateTime, granularity)
            .truncatedTo(ChronoUnit.SECONDS);
    }

    private LocalDateTime normalizeRequiredDateTime(LocalDateTime requiredDateTime, EDateGranularity granularity) {
        return switch (granularity) {
            case YEAR -> requiredDateTime.withMonth(12)
                    .withDayOfMonth(31)
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59);
            case MONTH ->
                    requiredDateTime.withDayOfMonth(requiredDateTime.toLocalDate().lengthOfMonth())
                            .withHour(23)
                            .withMinute(59)
                            .withSecond(59);
            case DAY ->
                    requiredDateTime.withHour(23)
                            .withMinute(59)
                            .withSecond(59);
            case HOUR ->
                    requiredDateTime.withMinute(59)
                            .withSecond(59);
            case MINUTE ->
                    requiredDateTime.withSecond(59);
            default ->
                    requiredDateTime;
        };
    }

}
