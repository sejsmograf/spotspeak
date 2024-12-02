package com.example.spotspeak.entity.notification;

import com.example.spotspeak.entity.User;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SingleUserNotificationEvent extends NotificationEvent {

    private User associatedUser;
}
