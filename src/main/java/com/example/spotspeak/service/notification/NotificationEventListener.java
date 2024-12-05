package com.example.spotspeak.service.notification;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.spotspeak.entity.notification.NotificationEvent;


@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) 
    public void onNotificationEvent(NotificationEvent event) {
        notificationService.createAndSendNotificationFromEvent(event);
    }
}
