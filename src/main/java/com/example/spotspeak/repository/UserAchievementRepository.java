package com.example.spotspeak.repository;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.entity.enumeration.EEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    @Query("""
        SELECT ua 
        FROM UserAchievement ua 
        WHERE ua.achievement.eventType = :eventType 
          AND ua.user = :user 
          AND ua.completedAt IS NULL
    """)
    List<UserAchievement> findUncompleted(
        @Param("eventType") EEventType eventType,
        @Param("user") User user
    );

    boolean existsByUserAndAchievement(User user, Achievement achievement);

    List<UserAchievement> findByUser(User user);

    @Query("""
    SELECT ua 
    FROM UserAchievement ua 
    WHERE ua.user = :user 
      AND ua.completedAt IS NOT NULL
    """)
    List<UserAchievement> findCompletedAchievementsByUser(@Param("user") User user);

    Optional<UserAchievement> findByIdAndUser(Long id, User user);

    @Query("""
    SELECT ua 
    FROM UserAchievement ua 
    WHERE ua.achievement = :achievement 
      AND ua.user = :user 
      AND ua.completedAt IS NOT NULL
    """)
    Optional<UserAchievement> findCompletedByAchievementAndUser(
        @Param("achievement") Achievement achievement,
        @Param("user") User user
    );
}
