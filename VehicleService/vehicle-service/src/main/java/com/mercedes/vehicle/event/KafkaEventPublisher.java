
package com.mercedes.vehicle.event;

import com.mercedes.vehicle.dto.VehicleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafka;

    @Value("${app.kafka.topic.vehicle-created}")
    private String vehicleCreatedTopic;

    @Value("${app.kafka.topic.vehicle-ownership-assigned}")
    private String ownershipTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void publishVehicleCreated(VehicleResponse vehicle) {

        Map<String, Object> event = new LinkedHashMap<>();

        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "vehicle.created");
        event.put("occurredAt", Instant.now());
        event.put("vehicleId", vehicle.vehicleId());
        event.put("vin", vehicle.vin());
        event.put("model", vehicle.model());
        event.put("modelId", vehicle.modelId());
        event.put("modelYear", vehicle.modelYear());
        event.put("selectedColor", vehicle.selectedColor());
        event.put("ownerId", vehicle.ownerId());
        event.put("ownerEmail", vehicle.ownerEmail());
        event.put("purchaseId", vehicle.purchaseId());

        kafka.send(
                vehicleCreatedTopic,
                vehicle.vehicleId(),
                event
        );
    }

    public void publishOwnershipAssigned(VehicleResponse vehicle) {

        Map<String, Object> event = new LinkedHashMap<>();

        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "vehicle.ownership.assigned");
        event.put("occurredAt", Instant.now());
        event.put("vehicleId", vehicle.vehicleId());
        event.put("vin", vehicle.vin());
        event.put("ownerId", vehicle.ownerId());
        event.put("ownerEmail", vehicle.ownerEmail());
        event.put("purchaseId", vehicle.purchaseId());
        event.put("model", vehicle.model());
        event.put("modelId", vehicle.modelId());
        event.put("selectedColor", vehicle.selectedColor());

        kafka.send(
                ownershipTopic,
                vehicle.vehicleId(),
                event
        );
    }
}
