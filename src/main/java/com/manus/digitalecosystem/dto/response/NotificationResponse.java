package com.manus.digitalecosystem.dto.response;

import com.manus.digitalecosystem.model.Notification;
import com.manus.digitalecosystem.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private NotificationType type;
    private String title;
    private String body;
    private String titleKey;
    private String bodyKey;
    private List<String> args;
    private boolean read;
    private Instant createdAt;

    public static NotificationResponse fromNotification(Notification notification, String title, String body) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(title)
                .body(body)
                .titleKey(notification.getTitleKey())
                .bodyKey(notification.getBodyKey())
                .args(notification.getArgs())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

