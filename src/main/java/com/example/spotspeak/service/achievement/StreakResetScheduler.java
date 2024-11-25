package com.example.spotspeak.service.achievement;

import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.repository.UserAchievementRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class StreakResetScheduler {

    private final UserAchievementRepository userAchievementRepository;

    public StreakResetScheduler(UserAchievementRepository userAchievementRepository) {
        this.userAchievementRepository = userAchievementRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetExpiredStreaks() {
        LocalDate today = LocalDate.now();

        List<UserAchievement> userAchievements = userAchievementRepository.findAllWithActiveStreaks();

        for (UserAchievement userAchievement : userAchievements) {
            LocalDate lastActionDate = userAchievement.getLastActionDate();

            if (lastActionDate != null && !lastActionDate.plusDays(1).equals(today)) {
                userAchievement.setCurrentStreak(0);
                userAchievement.setLastActionDate(null);
            }
        }

        userAchievementRepository.saveAll(userAchievements);
    }
}
