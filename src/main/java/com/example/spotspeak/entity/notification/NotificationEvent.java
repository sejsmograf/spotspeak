package com.example.spotspeak.entity.notification;

import java.util.Map;

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
    private Map<String, String> additionalData;
}
