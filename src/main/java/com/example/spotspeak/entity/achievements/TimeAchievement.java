package com.example.spotspeak.entity.achievements;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "time_achievements")
public class TimeAchievement extends Achievement {

    @Column(nullable = false)
    private LocalDateTime requiredTime;
}
