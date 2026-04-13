package com.manus.digitalecosystem.controller;

import com.manus.digitalecosystem.dto.response.NotificationResponse;
import com.manus.digitalecosystem.dto.response.PagedResponse;
import com.manus.digitalecosystem.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "User notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<PagedResponse<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false) Boolean read,
            Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(read, pageable));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.markRead(id, true));
    }

    @PatchMapping("/{id}/unread")
    @Operation(summary = "Mark notification as unread")
    public ResponseEntity<NotificationResponse> markUnread(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.markRead(id, false));
    }
}

