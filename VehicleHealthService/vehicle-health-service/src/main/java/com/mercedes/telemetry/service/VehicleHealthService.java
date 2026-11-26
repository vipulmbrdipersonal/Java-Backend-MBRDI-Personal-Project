package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.*;
import com.mercedes.telemetry.entity.HealthAlert;
import com.mercedes.telemetry.entity.VehicleHealthRecord;
import com.mercedes.telemetry.event.HealthEventPublisher;
import com.mercedes.telemetry.repository.HealthAlertRepository;
import com.mercedes.telemetry.repository.VehicleHealthRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class VehicleHealthService {

    private final VehicleHealthRepository healthRepository;
    private final HealthAlertRepository alertRepository;
    private final HealthAssessmentService assessmentService;
    private final HealthEventPublisher eventPublisher;

    public VehicleHealthService(
            VehicleHealthRepository healthRepository,
            HealthAlertRepository alertRepository,
            HealthAssessmentService assessmentService,
            HealthEventPublisher eventPublisher
    ) {
        this.healthRepository = healthRepository;
        this.alertRepository = alertRepository;
        this.assessmentService = assessmentService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void processTelemetry(TelemetryEvent telemetry) {

        if (telemetry.getEventId() == null
                || telemetry.getVehicleId() == null
                || telemetry.getVin() == null) {

            throw new IllegalArgumentException(
                    "Telemetry event is missing eventId, vehicleId or VIN"
            );
        }

        // Prevent duplicate processing when Kafka redelivers telemetry.
        if (healthRepository.existsByTelemetryEventId(
                telemetry.getEventId())) {
            return;
        }

        HealthAssessmentService.Assessment assessment =
                assessmentService.assess(telemetry);

        Instant assessedAt = telemetry.getRecordedAt() != null
                ? telemetry.getRecordedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                : Instant.now();

        VehicleHealthRecord record = new VehicleHealthRecord();

        record.setTelemetryEventId(telemetry.getEventId());
        record.setVehicleId(telemetry.getVehicleId());
        record.setVin(telemetry.getVin());

        record.setHealthScore(assessment.score());
        record.setStatus(assessment.status());
        record.setIssueSummary(String.join("; ", assessment.issues()));

        record.setEngineTemperature(telemetry.getEngineTemperature());
        record.setBatteryVoltage(telemetry.getBatteryVoltage());
        record.setFuelLevel(telemetry.getFuelLevel());

        record.setTyrePressureFrontLeft(
                telemetry.getTyrePressureFrontLeft()
        );

        record.setTyrePressureFrontRight(
                telemetry.getTyrePressureFrontRight()
        );

        record.setTyrePressureRearLeft(
                telemetry.getTyrePressureRearLeft()
        );

        record.setTyrePressureRearRight(
                telemetry.getTyrePressureRearRight()
        );

        record.setRpm(telemetry.getRpm());
        record.setAssessedAt(assessedAt);

        healthRepository.saveAndFlush(record);

        // Build the event shared with NotificationService.
        VehicleHealthEvent event = new VehicleHealthEvent(
                UUID.randomUUID().toString(),
                telemetry.getEventId(),
                telemetry.getVehicleId(),
                telemetry.getVin(),
                assessment.score(),
                assessment.status(),
                assessment.issues(),
                assessedAt
        );

        // Include telemetry readings for the email and PDF.
        event.setSpeed(telemetry.getSpeed());
        event.setEngineTemperature(telemetry.getEngineTemperature());
        event.setFuelLevel(telemetry.getFuelLevel());
        event.setBatteryVoltage(telemetry.getBatteryVoltage());
        event.setOdometer(telemetry.getOdometer());
        event.setRpm(telemetry.getRpm());

        event.setTyrePressureFrontLeft(
                telemetry.getTyrePressureFrontLeft()
        );

        event.setTyrePressureFrontRight(
                telemetry.getTyrePressureFrontRight()
        );

        event.setTyrePressureRearLeft(
                telemetry.getTyrePressureRearLeft()
        );

        event.setTyrePressureRearRight(
                telemetry.getTyrePressureRearRight()
        );

        event.setOwnerEmail(telemetry.getOwnerEmail());

        // Save individual warning and critical alerts.
        if (!assessment.issues().isEmpty()) {

            for (String issue : assessment.issues()) {

                String severity = issue.startsWith("CRITICAL:")
                        ? "CRITICAL"
                        : "WARNING";

                alertRepository.save(
                        new HealthAlert(
                                telemetry.getVehicleId(),
                                telemetry.getVin(),
                                severity,
                                issue,
                                telemetry.getEventId(),
                                assessedAt
                        )
                );
            }
        }

        // Publish every health assessment.
        // NotificationService consumes this topic and sends the report.
        eventPublisher.publishHealthUpdate(event);

        // Keep the existing alert flow separate.
        if (!assessment.issues().isEmpty()) {
            eventPublisher.publishAlert(event);
        }
    }

    @Transactional(readOnly = true)
    public HealthResponse getLatest(Long vehicleId) {

        VehicleHealthRecord record = healthRepository
                .findTopByVehicleIdOrderByAssessedAtDesc(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No health data found for vehicle " + vehicleId
                ));

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<HealthResponse> getHistory(
            Long vehicleId,
            org.springframework.data.domain.Pageable pageable
    ) {

        return healthRepository
                .findByVehicleIdOrderByAssessedAtDesc(
                        vehicleId,
                        pageable
                )
                .map(this::toResponse)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<HealthAlertResponse> getAlerts(
            Long vehicleId,
            org.springframework.data.domain.Pageable pageable
    ) {

        return alertRepository
                .findByVehicleIdOrderByCreatedAtDesc(
                        vehicleId,
                        pageable
                )
                .map(alert -> new HealthAlertResponse(
                        alert.getId(),
                        alert.getVehicleId(),
                        alert.getVin(),
                        alert.getSeverity(),
                        alert.getMessage(),
                        alert.getCreatedAt()
                ))
                .getContent();
    }

    private HealthResponse toResponse(VehicleHealthRecord record) {

        List<String> issues =
                record.getIssueSummary() == null
                        || record.getIssueSummary().isBlank()
                        ? List.of()
                        : List.of(record.getIssueSummary().split("; "));

        return new HealthResponse(
                record.getId(),
                record.getVehicleId(),
                record.getVin(),
                record.getTelemetryEventId(),
                record.getHealthScore(),
                record.getStatus(),
                issues,
                record.getAssessedAt()
        );
    }
}