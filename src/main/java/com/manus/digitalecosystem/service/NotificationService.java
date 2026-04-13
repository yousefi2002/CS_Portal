package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.response.NotificationResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.model.NotificationType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    void createNotification(String recipientUserId, NotificationType type, String titleKey, String bodyKey, List<String> args);

    PagedResponse<NotificationResponse> getMyNotifications(Boolean read, Pageable pageable);

    NotificationResponse markRead(String id, boolean read);
}

