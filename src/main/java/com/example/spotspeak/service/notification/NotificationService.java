package com.example.spotspeak.service.notification;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.notification.MultiUserNotificationEvent;
import com.example.spotspeak.entity.notification.NotificationEvent;
import com.example.spotspeak.entity.notification.SingleUserNotificationEvent;

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
            case ACHIEVEMENT_COMPLETED:
                sendAchievementCompletedMessage(event);
                break;

            default:
                break;
        }
    }

    private void sendTraceCommentedMessage(NotificationEvent event) {
        String traceId = event.getAdditionalData().get("traceId");
        String deepLink = "spotspeak:///user-traces?traceId=" + traceId;
        sendLocalizedNotification(event,
                deepLink,
                "trace.commented.title",
                "trace.commented.body");
    }

    private void sendUserMentionedMessage(NotificationEvent event) {
        String traceId = event.getAdditionalData().get("traceId");
        String deepLink = "spotspeak:///home/map?initialIndex=0&traceId=" + traceId;
        sendLocalizedNotification(event,
                deepLink,
                "user.mentioned.title",
                "user.mentioned.body");
    }

    private void sendFriendRequestReceivedMessage(NotificationEvent event) {
        String deepLink = "spotspeak:///home/friends-tab?initialIndex=2&initialTabIndex=1";
        sendLocalizedNotification(event,
                deepLink,
                "friend.request.received.title",
                "friend.request.received.body");
    }

    private void sendAchievementCompletedMessage(NotificationEvent event) {
        String deepLink = "spotspeak:///home/achievements";
        sendLocalizedNotification(event,
                deepLink,
                "achievement.completed.title",
                "achievement.completed.body");
    }

    private void sendLocalizedNotification(NotificationEvent event,
            String deepLink,
            String titleKey, String bodyKey) {
        // There is possibility to extend this method to support more languages
        // For now, only Polish is supported
        Locale polish = Locale.forLanguageTag("pl");
        String title = messageSource.getMessage(titleKey, null, polish);
        String body = messageSource.getMessage(bodyKey, null, polish);

        sendNotification(event, deepLink, title, body);
    }

    private void sendNotification(NotificationEvent event, String deepLink, String title, String body) {

        Map<String, String> data = new HashMap<>() {
            {
                put("deep_link", deepLink);
            }
        };

        if (event instanceof MultiUserNotificationEvent) {
            MultiUserNotificationEvent castedEvent = (MultiUserNotificationEvent) event;
            notificationSendingService.sendNotification(castedEvent.getAssociatedUsers(),
                    title, body, data);
        } else if (event instanceof SingleUserNotificationEvent) {
            SingleUserNotificationEvent castedEvent = (SingleUserNotificationEvent) event;
            notificationSendingService.sendNotification(castedEvent.getAssociatedUser(),
                    title, body, data);
        }
    }
}
