package com.example.spotspeak.entity.notification;

import java.util.List;

import com.example.spotspeak.entity.User;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class MultiUserNotificationEvent extends NotificationEvent {

    private List<User> associatedUsers;
}
