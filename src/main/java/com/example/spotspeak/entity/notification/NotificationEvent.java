package com.example.spotspeak.entity.notification;


import com.example.spotspeak.entity.enumeration.ENotificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class NotificationEvent {

    private ENotificationType type;
    private String description;
}
