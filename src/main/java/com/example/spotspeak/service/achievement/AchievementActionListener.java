package com.example.spotspeak.service.achievement;

import com.example.spotspeak.entity.achievements.Achievement;
import com.example.spotspeak.entity.achievements.UserAchievement;
import com.example.spotspeak.repository.UserAchievementRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AchievementActionListener {

    private final UserAchievementRepository userAchievementRepository;

    public AchievementActionListener(UserAchievementRepository userAchievementRepository) {
        this.userAchievementRepository = userAchievementRepository;
    }

    @EventListener
    public void onUserActionEvent(UserActionEvent event) {
        List<UserAchievement> userAchievements = userAchievementRepository
            .findUncompleted(event.getEventType(), event.getUser());

        for (UserAchievement userAchievement : userAchievements) {
            Achievement achievement = userAchievement.getAchievement();

            boolean allConditionsSatisfied =
                achievement.getConditions().isEmpty() || achievement.getConditions()
                .stream()
                .allMatch(condition -> condition.isSatisfied(event, userAchievement));

            if (allConditionsSatisfied) {
                userAchievement.setQuantityProgress(userAchievement.getQuantityProgress() + 1);

                if (userAchievement.getQuantityProgress() >= achievement.getRequiredQuantity()) {
                    userAchievement.setCompletedAt(LocalDateTime.now());
                }

                userAchievementRepository.save(userAchievement);
            }
        }
    }
}

