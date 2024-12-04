package com.example.spotspeak.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.ENotificationType;
import com.example.spotspeak.entity.notification.MultiUserNotificationEvent;
import com.example.spotspeak.entity.notification.NotificationEvent;
import com.example.spotspeak.entity.notification.SingleUserNotificationEvent;
import com.example.spotspeak.service.notification.NotificationEventListener;

/**
 * The {@code NotificationEventPublisher} class is responsible for publishing
 * domain-specific events.
 * 
 * These events are processed by listeners such as:
 * {@link NotificationEventListener}
 */
@Service
public class NotificationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public NotificationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCommentEvent(
            User associatedUser,
            String additionalDescription) {
        NotificationEvent event = SingleUserNotificationEvent.builder()
                .associatedUser(associatedUser)
                .type(ENotificationType.TRACE_COMMENTED)
                .build();

        publisher.publishEvent(event);
    }

    public void publishMentionEvent(
            List<User> associatedUsers,
            String additionalDescription) {
        NotificationEvent event = MultiUserNotificationEvent.builder()
                .associatedUsers(associatedUsers)
                .type(ENotificationType.USER_MENTIONED)
                .build();

        publisher.publishEvent(event);
    }

    public void publishFriendRequestEvent(
            User associatedUser,
            String additionalDescription) {
        NotificationEvent event = SingleUserNotificationEvent.builder()
                .associatedUser(associatedUser)
                .type(ENotificationType.FRIEND_REQUEST_RECEIVED)
                .build();

        publisher.publishEvent(event);
    }
}
