package com.example.spotspeak.service.achievement;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievements.Achievement;
import com.example.spotspeak.entity.achievements.Condition;
import com.example.spotspeak.entity.achievements.ConsecutiveDaysCondition;
import com.example.spotspeak.entity.achievements.TimeCondition;
import com.example.spotspeak.entity.achievements.LocationCondition;
import com.example.spotspeak.entity.achievements.UserAchievement;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.repository.AchievementRepository;
import com.example.spotspeak.repository.ConditionRepository;
import com.example.spotspeak.repository.UserAchievementRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AchievementService {

    private AchievementRepository achievementRepository;
    private ConditionRepository conditionRepository;
    private UserAchievementRepository userAchievementRepository;

    public AchievementService(AchievementRepository achievementRepository, ConditionRepository conditionRepository,
            UserAchievementRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.conditionRepository = conditionRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    public List<Achievement> getAllAchievements() {
        return (List<Achievement>) achievementRepository.findAll();
    }

    @Transactional
    public void createAchievement(String name, String description, int points, EEventType eventType,
            int requiredQuantity, List<Condition> conditions) {
        if (checkAchievementNotExists(name)) {
            Achievement achievement = Achievement.builder()
                    .name(name)
                    .description(description)
                    .points(points)
                    .eventType(eventType)
                    .requiredQuantity(requiredQuantity)
                    .build();

            if (conditions != null) {
                achievement.getConditions().addAll(conditions);
            }

            achievementRepository.save(achievement);
        }
    }

    private boolean checkAchievementNotExists(String name) {
        return achievementRepository.findByName(name) == null;
    }

    public ConsecutiveDaysCondition findOrCreateConsecutiveDaysCondition(int requiredDays) {
        ConsecutiveDaysCondition condition = ConsecutiveDaysCondition.builder()
                .requiredConsecutiveDays(requiredDays)
                .build();
        return conditionRepository.save(condition);
    }

    public LocationCondition findOrCreateLocationCondition(double latitude, double longitude, double radius) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        LocationCondition condition = LocationCondition.builder()
                .requiredLocation(location)
                .radiusInMeters(radius)
                .build();
        return conditionRepository.save(condition);
    }

    public TimeCondition createTimeCondition(LocalDateTime requiredDateTime,
            EDateGranularity granularity,
            LocalTime startTime,
            LocalTime endTime) {
        TimeCondition condition = TimeCondition.builder()
                .requiredDateTime(requiredDateTime)
                .granularity(granularity)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return conditionRepository.save(condition);
    }

    @Transactional
    public void initializeUserAchievements(User user) {
        List<Achievement> allAchievements = getAllAchievements();

        List<UserAchievement> newUserAchievements = allAchievements.stream()
                .filter(achievement -> !userAchievementRepository.existsByUserAndAchievement(user, achievement))
                .map(achievement -> UserAchievement.builder()
                        .user(user)
                        .achievement(achievement)
                        .quantityProgress(0)
                        .currentStreak(0)
                        .build())
                .toList();

        userAchievementRepository.saveAll(newUserAchievements);
    }
}
