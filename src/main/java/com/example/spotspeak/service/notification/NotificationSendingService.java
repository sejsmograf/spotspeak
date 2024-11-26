package com.example.spotspeak.service.notification;

import java.util.List;

import com.example.spotspeak.entity.User;

public interface NotificationSendingService {

    void sendNotification(User to, String title, String body);
    void sendNotification(List<User> to, String title, String body);
}
