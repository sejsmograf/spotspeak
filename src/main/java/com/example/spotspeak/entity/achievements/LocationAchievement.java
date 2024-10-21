package com.example.spotspeak.entity.achievements;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "location_achievements")
public class LocationAchievement extends Achievement {

    @Column(nullable = false)
    private Point requiredLocation;
}
