
package com.mercedes.telemetry.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.telemetry.dto.TelemetryEvent;
import com.mercedes.telemetry.service.VehicleHealthService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {

    private final ObjectMapper objectMapper;
    private final VehicleHealthService healthService;

    public TelemetryConsumer(ObjectMapper objectMapper,
                             VehicleHealthService healthService) {
        this.objectMapper = objectMapper;
        this.healthService = healthService;
    }

    @KafkaListener(
            topics = Topics.TELEMETRY_RAW,
            groupId = "vehicle-health-service-group"
    )
    public void consume(String message) throws Exception {
        TelemetryEvent event =
                objectMapper.readValue(message, TelemetryEvent.class);

        healthService.processTelemetry(event);
    }
}
