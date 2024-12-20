package com.example.spotspeak.service.achievement;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.repository.UserAchievementRepository;
import com.example.spotspeak.service.NotificationEventPublisher;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AchievementActionListener {

    private final UserAchievementRepository userAchievementRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    public AchievementActionListener(UserAchievementRepository userAchievementRepository,
            NotificationEventPublisher notificationEventPublisher) {
        this.userAchievementRepository = userAchievementRepository;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserActionEvent(UserActionEvent event) {
        List<UserAchievement> userAchievements = userAchievementRepository
                .findUncompleted(event.getEventType(), event.getUser());

        for (UserAchievement userAchievement : userAchievements) {
            Achievement achievement = userAchievement.getAchievement();

            boolean allConditionsSatisfied = achievement.getConditions().isEmpty() || achievement.getConditions()
                    .stream()
                    .allMatch(condition -> condition.isSatisfied(event, userAchievement));

            if (allConditionsSatisfied) {
                userAchievement.setQuantityProgress(userAchievement.getQuantityProgress() + 1);

                if (userAchievement.getQuantityProgress() >= achievement.getRequiredQuantity()) {
                    User associatedUser = userAchievement.getUser();
                    Long achievementId = userAchievement.getAchievement().getId();

                    userAchievement.setCompletedAt(LocalDateTime.now());
                    notificationEventPublisher.publishAchievementCompletedEvent(associatedUser, achievementId);
                }

                userAchievementRepository.save(userAchievement);
            }
        }
    }
}
