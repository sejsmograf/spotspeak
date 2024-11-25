package com.example.spotspeak.service;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.service.achievement.StreakResetScheduler;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StreakResetSchedulerIntegrationTest extends BaseServiceIntegrationTest {

    @Autowired
    private StreakResetScheduler streakResetScheduler;

    private User user;

    @BeforeEach
    public void setUp() {
        user = TestEntityFactory.createPersistedUser(entityManager);
        flushAndClear();
    }

    @Test
    @Transactional
    void shouldResetStreaksForUsersWhoDidNotPerformActionYesterday() {
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);

        UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithStreak(entityManager, user, 5, twoDaysAgo);
        flushAndClear();

        streakResetScheduler.resetExpiredStreaks();
        flushAndClear();

        UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
        assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(0);
        assertThat(updatedAchievement.getLastActionDate()).isNull();
    }

    @Test
    @Transactional
    void shouldNotResetStreaksForUsersWhoPerformedActionYesterday() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithStreak(entityManager, user, 5, yesterday);
        flushAndClear();

        streakResetScheduler.resetExpiredStreaks();
        flushAndClear();

        UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
        assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(5);
        assertThat(updatedAchievement.getLastActionDate()).isEqualTo(yesterday);
    }

    @Test
    @Transactional
    void shouldNotResetStreakForUsersWithNullLastActionDate() {
        UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithStreak(entityManager, user, 5, null);
        flushAndClear();

        streakResetScheduler.resetExpiredStreaks();
        flushAndClear();

        UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
        assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(5);
        assertThat(updatedAchievement.getLastActionDate()).isNull();
    }

}
