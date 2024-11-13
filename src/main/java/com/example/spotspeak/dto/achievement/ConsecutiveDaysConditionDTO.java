package com.example.spotspeak.dto.achievement;

import com.example.spotspeak.entity.achievement.Condition;
import com.example.spotspeak.entity.achievement.ConsecutiveDaysCondition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsecutiveDaysConditionDTO extends ConditionDTO {
    @NotNull private Integer requiredDays;

    @Override
    public Condition toCondition() {
        return ConsecutiveDaysCondition.builder()
            .requiredConsecutiveDays(requiredDays)
            .build();
    }
}