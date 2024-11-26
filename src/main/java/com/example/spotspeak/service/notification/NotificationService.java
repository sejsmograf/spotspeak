package com.example.spotspeak.service.notification;

import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.NotificationEvent;

@Service
public class NotificationService {

    private final NotificationSendingService notificationSendingService;

    public NotificationService(NotificationSendingService notificationSendingService) {
        this.notificationSendingService = notificationSendingService;
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
        String title = "Nowy komentarz";
        String body = "Twój ślad został skomentowany";
        notificationSendingService.sendNotification(event.getAssociatedUser(), title, body);
    }

    private void sendUserMentionedMessage(NotificationEvent event) {
        String title = "Nowe wzmianki";
        String body = "Zostałeś oznaczony w komentarzu";

        notificationSendingService.sendNotification(event.getAssociatedUser(), title, body);
    }

    private void sendFriendRequestReceivedMessage(NotificationEvent event) {
        String title = "Nowe zaproszenie do znajomych";
        String body = "Otrzymałeś zaproszenie do znajomych";

        notificationSendingService.sendNotification(event.getAssociatedUser(), title, body);
    }

}
