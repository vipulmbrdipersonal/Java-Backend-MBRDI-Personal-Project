
package com.mercedes.telemetry.controller;

import com.mercedes.telemetry.dto.NotificationResponse;
import com.mercedes.telemetry.service.NotificationManagementService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationManagementService notificationService;

    public NotificationController(
            NotificationManagementService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<NotificationResponse> getNotifications(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100)
        );

        return notificationService.getNotifications(
                vehicleId, unreadOnly, pageable);
    }

    @GetMapping("/vehicle/{vehicleId}/unread-count")
    public Map<String, Object> getUnreadCount(
            @PathVariable Long vehicleId) {

        return Map.of(
                "vehicleId", vehicleId,
                "unreadCount",
                notificationService.getUnreadCount(vehicleId)
        );
    }

    @PatchMapping("/vehicle/{vehicleId}/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long vehicleId,
            @PathVariable Long notificationId) {

        return notificationService.markAsRead(vehicleId, notificationId);
    }

    @PatchMapping("/vehicle/{vehicleId}/read-all")
    public Map<String, Object> markAllAsRead(
            @PathVariable Long vehicleId) {

        int updated = notificationService.markAllAsRead(vehicleId);

        return Map.of(
                "vehicleId", vehicleId,
                "updatedCount", updated,
                "message", "Notifications marked as read"
        );
    }
}
