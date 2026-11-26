
package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.*;
import com.mercedes.telemetry.entity.Notification;
import com.mercedes.telemetry.event.NotificationEventPublisher;
import com.mercedes.telemetry.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationManagementService {

    private final NotificationRepository repository;
    private final NotificationEventPublisher eventPublisher;

    public NotificationManagementService(
            NotificationRepository repository,
            NotificationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void processHealthAlert(VehicleHealthEvent event) {

        if (event.getEventId() == null ||
                event.getVehicleId() == null ||
                event.getVin() == null) {
            throw new IllegalArgumentException(
                    "Health event is missing eventId, vehicleId or VIN");
        }

        if (event.getIssues() == null || event.getIssues().isEmpty()) {
            return;
        }

        Instant createdAt = event.getAssessedAt() != null
                ? event.getAssessedAt()
                : Instant.now();

        for (int index = 0; index < event.getIssues().size(); index++) {

            if (repository.existsBySourceEventIdAndIssueIndex(
                    event.getEventId(), index)) {
                continue;
            }

            String issue = event.getIssues().get(index);
            String severity = issue.startsWith("CRITICAL:")
                    ? "CRITICAL" : "WARNING";

            String title = severity.equals("CRITICAL")
                    ? "Critical vehicle health alert"
                    : "Vehicle health warning";

            Notification notification = repository.saveAndFlush(
                    new Notification(
                            event.getEventId(),
                            index,
                            event.getVehicleId(),
                            event.getVin(),
                            severity,
                            title,
                            issue,
                            createdAt
                    )
            );

            NotificationCreatedEvent createdEvent =
                    new NotificationCreatedEvent(
                            UUID.randomUUID().toString(),
                            notification.getId(),
                            notification.getVehicleId(),
                            notification.getVin(),
                            notification.getSeverity(),
                            notification.getTitle(),
                            notification.getMessage(),
                            notification.isRead(),
                            notification.getCreatedAt()
                    );

            eventPublisher.publish(createdEvent);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(
            Long vehicleId,
            Boolean unreadOnly,
            Pageable pageable) {

        Page<Notification> result;

        if (Boolean.TRUE.equals(unreadOnly)) {
            result = repository.findByVehicleIdAndReadOrderByCreatedAtDesc(
                    vehicleId, false, pageable);
        } else {
            result = repository.findByVehicleIdOrderByCreatedAtDesc(
                    vehicleId, pageable);
        }

        return result.map(this::toResponse).getContent();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long vehicleId) {
        return repository.countByVehicleIdAndReadFalse(vehicleId);
    }

    @Transactional
    public NotificationResponse markAsRead(
            Long vehicleId, Long notificationId) {

        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found"));

        if (!notification.getVehicleId().equals(vehicleId)) {
            throw new IllegalArgumentException(
                    "Notification does not belong to this vehicle");
        }

        notification.markAsRead();

        return toResponse(repository.save(notification));
    }

    @Transactional
    public int markAllAsRead(Long vehicleId) {
        List<Notification> notifications = repository
                .findByVehicleIdAndReadOrderByCreatedAtDesc(
                        vehicleId, false, Pageable.unpaged())
                .getContent();

        notifications.forEach(Notification::markAsRead);
        repository.saveAll(notifications);

        return notifications.size();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getVehicleId(),
                notification.getVin(),
                notification.getSeverity(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
