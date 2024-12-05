package com.example.spotspeak.entity.achievement;

import com.example.spotspeak.dto.achievement.LocationConditionDTO;
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
import org.locationtech.jts.geom.Polygon;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "location_conditions")
public class LocationCondition extends Condition {

    @Column(nullable = false, columnDefinition = "geometry(Polygon, 4326)")
    private Polygon region;

    @Override
    public boolean isSatisfied(UserActionEvent event, UserAchievement userAchievement) {
        Point location = event.getLocation();

        if(location == null) {
            return false;
        }
        return region.contains(location);
    }

    @Override
    public LocationConditionDTO toDTO() {
        return new LocationConditionDTO(
            this.region.toText()
            );
    }
}
