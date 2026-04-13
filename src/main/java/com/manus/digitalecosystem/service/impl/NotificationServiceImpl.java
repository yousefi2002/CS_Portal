package com.manus.digitalecosystem.service.impl;

import com.manus.digitalecosystem.dto.response.NotificationResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Notification;
import com.manus.digitalecosystem.model.NotificationType;
import com.manus.digitalecosystem.repository.NotificationRepository;
import com.manus.digitalecosystem.service.NotificationService;
import com.manus.digitalecosystem.util.SecurityUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MessageSource messageSource;

    public NotificationServiceImpl(NotificationRepository notificationRepository, MessageSource messageSource) {
        this.notificationRepository = notificationRepository;
        this.messageSource = messageSource;
    }

    @Override
    public void createNotification(String recipientUserId, NotificationType type, String titleKey, String bodyKey, List<String> args) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .titleKey(titleKey)
                .bodyKey(bodyKey)
                .args(args)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(Boolean read, Pageable pageable) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Page<Notification> page;
        if (read == null) {
            page = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(currentUserId, pageable);
        } else {
            page = notificationRepository.findByRecipientUserIdAndReadOrderByCreatedAtDesc(currentUserId, read, pageable);
        }

        Locale locale = LocaleContextHolder.getLocale();
        return PagedResponse.fromPage(page.map(notification -> {
            Object[] args = notification.getArgs() == null ? null : notification.getArgs().toArray();
            String title = messageSource.getMessage(notification.getTitleKey(), args, notification.getTitleKey(), locale);
            String body = messageSource.getMessage(notification.getBodyKey(), args, notification.getBodyKey(), locale);
            return NotificationResponse.fromNotification(notification, title, body);
        }));
    }

    @Override
    public NotificationResponse markRead(String id, boolean read) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndRecipientUserId(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notification.not_found", id));

        notification.setRead(read);
        Notification saved = notificationRepository.save(notification);

        Locale locale = LocaleContextHolder.getLocale();
        Object[] args = saved.getArgs() == null ? null : saved.getArgs().toArray();
        String title = messageSource.getMessage(saved.getTitleKey(), args, saved.getTitleKey(), locale);
        String body = messageSource.getMessage(saved.getBodyKey(), args, saved.getBodyKey(), locale);

        return NotificationResponse.fromNotification(saved, title, body);
    }
}

