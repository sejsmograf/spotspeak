package com.example.spotspeak.entity.achievements;

import com.example.spotspeak.dto.ConsecutiveDaysConditionDTO;
import com.example.spotspeak.service.achievement.UserActionEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "consecutive_days_conditions")
public class ConsecutiveDaysCondition extends Condition{

    @Column(nullable = false)
    private Integer requiredConsecutiveDays;

    @Override
    public boolean isSatisfied(UserActionEvent event, UserAchievement userAchievement) {
        LocalDate today = event.getTimestamp().toLocalDate();
        LocalDate lastActionDate = userAchievement.getLastActionDate();

        if(today == null) {
            return false;
        }

        if (lastActionDate != null && lastActionDate.plusDays(1).equals(today)) {
            userAchievement.setCurrentStreak(userAchievement.getCurrentStreak() + 1);
        } else if (lastActionDate == null || !lastActionDate.equals(today)) {
            userAchievement.setCurrentStreak(1);
        }

        userAchievement.setLastActionDate(today);

        return userAchievement.getCurrentStreak() >= requiredConsecutiveDays;
    }

    @Override
    public ConsecutiveDaysConditionDTO toDTO() {
        return new ConsecutiveDaysConditionDTO(
            "ConsecutiveDaysCondition",
            this.requiredConsecutiveDays
        );
    }
}
