package com.example.spotspeak.dto.achievement;

import com.example.spotspeak.entity.achievement.Condition;
import com.example.spotspeak.entity.achievement.LocationCondition;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationConditionDTO extends ConditionDTO {
    @NotBlank private String region;

    @Override
    public Condition toCondition() {
        GeometryFactory geometryFactory = new GeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);

        try {
            Polygon polygon = (Polygon) reader.read(this.region);
            return LocationCondition.builder()
                .region(polygon)
                .build();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid polygon format", e);
        }
    }
}
