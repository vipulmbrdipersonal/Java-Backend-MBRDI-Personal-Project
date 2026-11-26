
package com.mercedes.dashboard.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.dashboard.dto.VehicleHealthEvent;
import com.mercedes.dashboard.entity.VehicleHealthSnapshot;
import com.mercedes.dashboard.repository.VehicleHealthSnapshotRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class HealthUpdatedConsumer {

    private final ObjectMapper objectMapper;
    private final VehicleHealthSnapshotRepository repository;

    public HealthUpdatedConsumer(
            ObjectMapper objectMapper,
            VehicleHealthSnapshotRepository repository) {

        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(
            topics = "vehicle.health.updated",
            groupId = "dashboard-service-group"
    )
    @Transactional
    public void consume(String message) {

        try {

            VehicleHealthEvent event =
                    objectMapper.readValue(message, VehicleHealthEvent.class);

            if (event.getVehicleId() == null) {
                throw new IllegalArgumentException(
                        "Vehicle ID is missing in health event"
                );
            }

            // Convert Instant from Kafka event to LocalDateTime for the entity.
            LocalDateTime assessedAt = null;

            Instant eventTimestamp = event.getAssessedAt();

            if (eventTimestamp != null) {
                assessedAt = LocalDateTime.ofInstant(
                        eventTimestamp,
                        ZoneOffset.UTC
                );
            }

            // Find existing vehicle health snapshot.
            VehicleHealthSnapshot snapshot =
                    repository.findById(event.getVehicleId()).orElse(null);

            // Create snapshot if it does not exist.
            if (snapshot == null) {
                snapshot = new VehicleHealthSnapshot(event.getVehicleId());
            }

            // Ignore older events to prevent outdated data from overwriting newer data.
            if (snapshot.getAssessedAt() != null
                    && assessedAt != null
                    && assessedAt.isBefore(snapshot.getAssessedAt())) {

                return;
            }

            snapshot.setTelemetryEventId(event.getTelemetryEventId());
            snapshot.setVin(event.getVin());
            snapshot.setHealthScore(event.getHealthScore());
            snapshot.setStatus(event.getStatus());

            if (event.getIssues() != null) {
                snapshot.setIssueSummary(
                        String.join("; ", event.getIssues())
                );
            } else {
                snapshot.setIssueSummary("");
            }

            snapshot.setAssessedAt(assessedAt);
            snapshot.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

            repository.save(snapshot);

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Failed to process vehicle.health.updated event",
                    exception
            );
        }
    }
}
