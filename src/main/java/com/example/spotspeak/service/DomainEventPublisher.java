package com.example.spotspeak.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.ENotificationType;
import com.example.spotspeak.entity.notification.MultiUserNotificationEvent;
import com.example.spotspeak.entity.notification.NotificationEvent;
import com.example.spotspeak.entity.notification.SingleUserNotificationEvent;

@Service
public class DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public DomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishNotificationEvent(
            User associatedUser,
            ENotificationType type,
            String additionalDescription) {
        NotificationEvent event = SingleUserNotificationEvent.builder()
                .associatedUser(associatedUser)
                .type(ENotificationType.TRACE_COMMENTED)
                .build();

        publisher.publishEvent(event);
    }

    public void publishNotificationEvent(
            List<User> associatedUsers,
            ENotificationType type,
            String additionalDescription) {
        NotificationEvent event = MultiUserNotificationEvent.builder()
                .associatedUsers(associatedUsers)
                .type(ENotificationType.TRACE_COMMENTED)
                .build();

        publisher.publishEvent(event);
    }
}
