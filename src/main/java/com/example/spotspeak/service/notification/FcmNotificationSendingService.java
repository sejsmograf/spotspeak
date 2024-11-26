package com.example.spotspeak.service.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class FcmNotificationSendingService implements NotificationSendingService {

    private final Logger logger = LoggerFactory.getLogger(FcmNotificationSendingService.class);
    private final FirebaseMessaging firebaseMessaging;

    public FcmNotificationSendingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public void sendNotification(User to, String title, String body) {
        if (to.getFcmToken() == null) {
            logger.warn("User {} has no FCM token", to.getId());
            return;
        }
        String token = to.getFcmToken();

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setNotification(notification)
                .setToken(token)
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send notification to user {}", to.getId(), e);
            return;
        }
    }

    @Override
    public void sendNotification(List<User> to, String title, String body) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendNotification'");
    }

}