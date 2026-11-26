
package com.mercedes.telemetry.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.telemetry.dto.VehicleHealthEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class HealthEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public HealthEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishHealthUpdate(VehicleHealthEvent event) {
        publish(Topics.HEALTH_UPDATED, event);
    }

    public void publishAlert(VehicleHealthEvent event) {
        publish(Topics.HEALTH_ALERT, event);
    }

    private void publish(String topic, VehicleHealthEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(event.getVehicleId()), json);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize vehicle health event", ex);
        }
    }
}
