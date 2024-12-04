package com.example.spotspeak.service.notification;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.User;

@Service
@Profile({ "test", "local" })
public class MockNotificationService implements NotificationSendingService {

    private final Logger logger = LoggerFactory.getLogger(MockNotificationService.class);

    @Override
    public void sendNotification(User to, String title, String body, Map<String, String> additionalData) {
        logger.info("""
                Sending notification to user: %s
                Title: %s
                Body: %s
                Additional data: %s
                """.formatted(to, title, body, additionalData));
    }

    @Override
    public void sendNotification(List<User> to, String title, String body, Map<String, String> additionalData) {
        logger.info("""
                Sending notification to users: %s
                Title: %s
                Body: %s
                Additional data: %s
                """.formatted(to, title, body, additionalData));
    }

}
