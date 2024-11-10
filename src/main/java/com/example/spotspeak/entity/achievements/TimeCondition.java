package com.example.spotspeak.entity.achievements;

import com.example.spotspeak.dto.TimeConditionDTO;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import com.example.spotspeak.service.achievement.UserActionEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "time_conditions")
public class TimeCondition extends Condition {

    @Column(nullable = false)
    private LocalDateTime requiredDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EDateGranularity granularity;

    @Column(nullable = true)
    private LocalTime startTime;

    @Column(nullable = true)
    private LocalTime endTime;

    @Override
    public boolean isSatisfied(UserActionEvent event, UserAchievement userAchievement) {
        LocalDateTime eventDateTime = event.getTimestamp();
        if (eventDateTime == null) {
            return false;
        }

        LocalTime eventTime = LocalTime.of(eventDateTime.getHour(), eventDateTime.getMinute());

        return switch (granularity) {
            case YEAR -> isSameYear(eventDateTime);
            case MONTH -> isSameMonth(eventDateTime);
            case DAY -> isSameDay(eventDateTime);
            case HOUR -> isSameHour(eventDateTime);
            case MINUTE -> requiredDateTime.equals(eventDateTime.truncatedTo(ChronoUnit.MINUTES));
            case ONLY_HOUR -> isSameOnlyHour(eventDateTime);
            case TIME_RANGE -> isBetweenHours(eventTime);
        };
    }

    @Override
    public TimeConditionDTO toDTO() {
        return new TimeConditionDTO(
            "TimeCondition",
            this.requiredDateTime,
            this.startTime,
            this.endTime
        );
    }

    private boolean isSameYear(LocalDateTime eventDateTime) {
        return requiredDateTime.getYear() == eventDateTime.getYear();
    }

    private boolean isSameMonth(LocalDateTime eventDateTime) {
        return isSameYear(eventDateTime) && requiredDateTime.getMonth() == eventDateTime.getMonth();
    }

    private boolean isSameDay(LocalDateTime eventDateTime) {
        return isSameMonth(eventDateTime) && requiredDateTime.getDayOfMonth() == eventDateTime.getDayOfMonth();
    }

    private boolean isSameHour(LocalDateTime eventDateTime) {
        return isSameDay(eventDateTime) && requiredDateTime.getHour() == eventDateTime.getHour();
    }

    private boolean isSameOnlyHour(LocalDateTime eventDateTime) {
        return requiredDateTime.getHour() == eventDateTime.getHour();
    }

    private boolean isBetweenHours(LocalTime eventTime) {
        if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            return (eventTime.equals(startTime) || eventTime.isAfter(startTime)) &&
                (eventTime.equals(endTime) || eventTime.isBefore(endTime));
        }

        return (eventTime.equals(startTime) || eventTime.isAfter(startTime)) ||
            (eventTime.equals(endTime) || eventTime.isBefore(endTime));
    }
}
