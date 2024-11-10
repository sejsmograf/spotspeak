package com.example.spotspeak.service.achievement;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserActionEvent {
    private User user;
    private EEventType eventType;
    private Point location;
    private LocalDateTime timestamp;
}