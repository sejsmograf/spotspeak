package com.example.spotspeak.dto;

import com.google.firebase.database.annotations.NotNull;


public record NotificationPreferencesDTO(
        @NotNull boolean receiveNotifications) {
}
