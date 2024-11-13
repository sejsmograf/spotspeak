package com.example.spotspeak.repository;

import com.example.spotspeak.entity.achievement.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Achievement findByName(String name);
}


