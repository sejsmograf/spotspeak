package com.example.spotspeak.repository;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievements.Achievement;
import com.example.spotspeak.entity.achievements.UserAchievement;
import com.example.spotspeak.entity.enumeration.EEventType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAchievementRepository extends CrudRepository<UserAchievement, Long> {

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
}
