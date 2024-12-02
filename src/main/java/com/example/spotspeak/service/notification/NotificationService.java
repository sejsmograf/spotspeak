package com.example.spotspeak.service.notification;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.NotificationEvent;

@Service
public class NotificationService {

    private final NotificationSendingService notificationSendingService;
    private final MessageSource messageSource;

    public NotificationService(NotificationSendingService notificationSendingService,
            MessageSource messageSource) {
        this.notificationSendingService = notificationSendingService;
        this.messageSource = messageSource;
    }

    public void createAndSendNotificationFromEvent(NotificationEvent event) {
        switch (event.getType()) {
            case TRACE_COMMENTED:
                sendTraceCommentedMessage(event);
                break;
            case USER_MENTIONED:
                sendUserMentionedMessage(event);
                break;
            case FRIEND_REQUEST_RECEIVED:
                sendFriendRequestReceivedMessage(event);
                break;

            default:
                break;
        }
    }

    private void sendTraceCommentedMessage(NotificationEvent event) {
        sendLocalizedNotification(event,
                "trace.commented.title",
                "trace.commented.body");
    }

    private void sendUserMentionedMessage(NotificationEvent event) {
        sendLocalizedNotification(event,
                "user.mentioned.title",
                "user.mentioned.body");
    }

    private void sendFriendRequestReceivedMessage(NotificationEvent event) {
        sendLocalizedNotification(event,
                "friend.request.received.title",
                "friend.request.received.body");
    }

    private void sendLocalizedNotification(NotificationEvent event, String titleKey, String bodyKey) {
        // There is possibility to extend this method to support more languages
        // For now, only Polish is supported
        Locale polish = new Locale("pl");
        String title = messageSource.getMessage(titleKey, null, polish);
        String body = messageSource.getMessage(bodyKey, null, polish);

        notificationSendingService.sendNotification(event.getAssociatedUser(), title, body);
    }
}
