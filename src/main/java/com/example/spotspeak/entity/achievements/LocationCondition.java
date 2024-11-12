package com.example.spotspeak.entity.achievements;

import com.example.spotspeak.dto.LocationConditionDTO;
import com.example.spotspeak.service.achievement.UserActionEvent;
import lombok.EqualsAndHashCode;
import org.locationtech.jts.geom.Point;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "location_conditions")
public class LocationCondition extends Condition {

    @Column(nullable = false)
    private Point requiredLocation;

    @Column(nullable = false)
    private double radiusInMeters;

    @Override
    public boolean isSatisfied(UserActionEvent event, UserAchievement userAchievement) {
        Point location = event.getLocation();

        if(location == null) {
            return false;
        }
        double distance = location.distance(requiredLocation);
        return distance <= radiusInMeters;
    }

    @Override
    public LocationConditionDTO toDTO() {
        return new LocationConditionDTO(
            "LocationCondition",
            this.radiusInMeters,
            this.requiredLocation.getX(),
            this.requiredLocation.getY()
            );
    }
}
