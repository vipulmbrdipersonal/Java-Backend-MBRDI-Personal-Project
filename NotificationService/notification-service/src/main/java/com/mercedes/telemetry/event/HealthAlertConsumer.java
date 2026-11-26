
package com.mercedes.telemetry.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.telemetry.dto.VehicleHealthEvent;
import com.mercedes.telemetry.service.NotificationManagementService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HealthAlertConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationManagementService notificationService;

    public HealthAlertConsumer(
            ObjectMapper objectMapper,
            NotificationManagementService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = Topics.HEALTH_ALERT,
            groupId = "notification-service-group"
    )
    public void consume(String message) throws Exception {

        VehicleHealthEvent event = objectMapper.readValue(
                message, VehicleHealthEvent.class);

        notificationService.processHealthAlert(event);
    }
}
