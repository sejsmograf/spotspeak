package com.example.spotspeak.entity;


import com.example.spotspeak.entity.enumeration.ENotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private User associatedUser;
    private ENotificationType type;
    private String description;
}
