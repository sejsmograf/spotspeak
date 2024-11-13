package com.example.spotspeak.dto.achievement;

import com.example.spotspeak.entity.achievement.Condition;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConsecutiveDaysConditionDTO.class, name = "ConsecutiveDaysCondition"),
    @JsonSubTypes.Type(value = LocationConditionDTO.class, name = "LocationCondition"),
    @JsonSubTypes.Type(value = TimeConditionDTO.class, name = "TimeCondition")
})
public abstract class ConditionDTO {
    public abstract Condition toCondition();
}
