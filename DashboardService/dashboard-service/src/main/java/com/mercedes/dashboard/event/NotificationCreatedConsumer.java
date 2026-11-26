
package com.mercedes.dashboard.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.dashboard.dto.NotificationCreatedEvent;
import com.mercedes.dashboard.entity.DashboardNotification;
import com.mercedes.dashboard.repository.DashboardNotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NotificationCreatedConsumer {

    private final ObjectMapper objectMapper;
    private final DashboardNotificationRepository repository;

    public NotificationCreatedConsumer(
            ObjectMapper objectMapper,
            DashboardNotificationRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(
            topics = Topics.NOTIFICATION_CREATED,
            groupId = "dashboard-notification-service-group"
    )
    public void consume(String message) throws Exception {

        NotificationCreatedEvent event =
                objectMapper.readValue(
                        message, NotificationCreatedEvent.class);

        if (event.getNotificationId() == null ||
                event.getVehicleId() == null ||
                event.getVin() == null) {
            throw new IllegalArgumentException(
                    "Notification event is missing required fields");
        }

        if (repository.existsBySourceNotificationId(
                event.getNotificationId())) {
            return;
        }

        DashboardNotification notification =
                new DashboardNotification(
                        event.getNotificationId(),
                        event.getVehicleId(),
                        event.getVin(),
                        event.getSeverity(),
                        event.getTitle(),
                        event.getMessage(),
                        event.isRead(),
                        event.getCreatedAt() != null
                                ? event.getCreatedAt()
                                : Instant.now()
                );

        repository.save(notification);
    }
}
