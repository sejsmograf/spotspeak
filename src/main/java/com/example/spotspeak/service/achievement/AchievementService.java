package com.example.spotspeak.service.achievement;

import com.example.spotspeak.entity.achievements.Achievement;
import com.example.spotspeak.entity.achievements.Condition;
import com.example.spotspeak.entity.achievements.ConsecutiveDaysCondition;
import com.example.spotspeak.entity.achievements.TimeCondition;
import com.example.spotspeak.entity.achievements.LocationCondition;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.repository.AchievementRepository;
import com.example.spotspeak.repository.ConditionRepository;
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

    public AchievementService(AchievementRepository achievementRepository, ConditionRepository conditionRepository) {
        this.achievementRepository = achievementRepository;
        this.conditionRepository = conditionRepository;
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
}
