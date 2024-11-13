package com.example.spotspeak.dto.achievement;

import com.example.spotspeak.entity.achievement.Condition;
import com.example.spotspeak.entity.achievement.TimeCondition;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeConditionDTO extends ConditionDTO {
    @NotNull private LocalDateTime requiredDateTime;
    @NotBlank private String granularity;
    private LocalTime startTime;
    private LocalTime endTime;

    @Override
    public Condition toCondition() {
        return TimeCondition.builder()
            .requiredDateTime(requiredDateTime)
            .granularity(EDateGranularity.valueOf(granularity.toUpperCase()))
            .startTime(startTime)
            .endTime(endTime)
            .build();
    }

}

