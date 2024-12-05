package com.example.spotspeak.entity.achievement;

import com.example.spotspeak.service.achievement.UserActionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "conditions")
public abstract class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public abstract boolean isSatisfied(UserActionEvent event, UserAchievement userAchievement);

    public abstract Object toDTO();
}
