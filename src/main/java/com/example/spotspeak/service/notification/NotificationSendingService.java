package com.example.spotspeak.service.notification;

import java.util.List;
import java.util.Map;

import com.example.spotspeak.entity.User;

public interface NotificationSendingService {

    void sendNotification(User to, String title, String body, Map<String, String> additionalData);

    void sendNotification(List<User> to, String title, String body, Map<String, String> additionalData);
}
