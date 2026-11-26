package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.TelemetryEvent;
import com.mercedes.telemetry.dto.TelemetryRequest;
import com.mercedes.telemetry.dto.TelemetryResponse;
import com.mercedes.telemetry.dto.VehicleDetails;
import com.mercedes.telemetry.entity.TelemetryRecord;
import com.mercedes.telemetry.event.TelemetryEventPublisher;
import com.mercedes.telemetry.repository.TelemetryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TelemetryService {

    private final TelemetryRepository repository;
    private final TelemetryEventPublisher eventPublisher;
    private final VehicleServiceClient vehicleServiceClient;

    public TelemetryService(
            TelemetryRepository repository,
            TelemetryEventPublisher eventPublisher,
            VehicleServiceClient vehicleServiceClient
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.vehicleServiceClient = vehicleServiceClient;
    }

    /*
     * Existing manual ingestion endpoint.
     */
    @Transactional
    public TelemetryResponse ingest(TelemetryRequest request) {
        return saveTelemetry(request, null);
    }

    /*
     * Generate fresh telemetry after verifying vehicle ownership.
     */
    @Transactional
    public TelemetryResponse simulate(
            String vehicleCode,
            String authorizationHeader
    ) {
        VehicleDetails vehicle =
                vehicleServiceClient.getOwnedVehicle(
                        vehicleCode,
                        authorizationHeader
                );

        TelemetryRequest request = generateRandomTelemetry(vehicle);

        return saveTelemetry(request, vehicle.ownerEmail());
    }

    /*
     * Save telemetry and publish its Kafka event.
     */
    private TelemetryResponse saveTelemetry(
            TelemetryRequest request,
            String ownerEmail
    ) {
        validateTelemetry(request);

        TelemetryRecord record = new TelemetryRecord();

        String eventId = UUID.randomUUID().toString();

        record.setEventId(eventId);
        record.setVehicleId(request.vehicleId());
        record.setVin(request.vin().trim().toUpperCase());
        record.setSpeed(request.speed());
        record.setEngineTemperature(request.engineTemperature());
        record.setFuelLevel(request.fuelLevel());
        record.setBatteryVoltage(request.batteryVoltage());
        record.setOdometer(request.odometer());
        record.setRpm(request.rpm());
        record.setTyrePressureFrontLeft(
                request.tyrePressureFrontLeft()
        );
        record.setTyrePressureFrontRight(
                request.tyrePressureFrontRight()
        );
        record.setTyrePressureRearLeft(
                request.tyrePressureRearLeft()
        );
        record.setTyrePressureRearRight(
                request.tyrePressureRearRight()
        );
        record.setRecordedAt(LocalDateTime.now());

        TelemetryRecord saved = repository.saveAndFlush(record);

        TelemetryEvent event = new TelemetryEvent(
                saved.getEventId(),
                saved.getId(),
                saved.getVehicleId(),
                saved.getVin(),
                saved.getSpeed(),
                saved.getEngineTemperature(),
                saved.getFuelLevel(),
                saved.getBatteryVoltage(),
                saved.getOdometer(),
                saved.getRpm(),
                saved.getTyrePressureFrontLeft(),
                saved.getTyrePressureFrontRight(),
                saved.getTyrePressureRearLeft(),
                saved.getTyrePressureRearRight(),
                saved.getRecordedAt(),
                ownerEmail
        );

        eventPublisher.publish(event);

        return toResponse(saved);
    }

    /*
     * Randomly generate one of three health scenarios.
     */
    private TelemetryRequest generateRandomTelemetry(
            VehicleDetails vehicle
    ) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int scenario = random.nextInt(3);

        BigDecimal speed = decimal(random.nextDouble(20, 140));

        BigDecimal temperature;
        BigDecimal fuel;
        BigDecimal voltage;
        BigDecimal rpm;

        BigDecimal frontLeft = decimal(random.nextDouble(30, 38));
        BigDecimal frontRight = decimal(random.nextDouble(30, 38));
        BigDecimal rearLeft = decimal(random.nextDouble(30, 38));
        BigDecimal rearRight = decimal(random.nextDouble(30, 38));

        switch (scenario) {

            case 0 -> {
                // HEALTHY
                temperature = decimal(random.nextDouble(80, 100));
                fuel = decimal(random.nextDouble(20, 95));
                voltage = decimal(random.nextDouble(12.2, 14.8));
                rpm = BigDecimal.valueOf(
                        random.nextInt(1000, 5500)
                );
            }

            case 1 -> {
                // WARNING
                temperature = decimal(random.nextDouble(106, 115));
                fuel = decimal(random.nextDouble(20, 95));
                voltage = decimal(random.nextDouble(12.2, 14.8));
                rpm = BigDecimal.valueOf(
                        random.nextInt(1000, 5500)
                );
            }

            case 2 -> {
                // CRITICAL
                temperature = decimal(random.nextDouble(121, 145));
                fuel = decimal(random.nextDouble(20, 95));
                voltage = decimal(random.nextDouble(12.2, 14.8));
                rpm = BigDecimal.valueOf(
                        random.nextInt(1000, 5500)
                );
            }

            default -> throw new IllegalStateException(
                    "Unexpected telemetry scenario"
            );
        }

        Long previousOdometer = repository
                .findFirstByVehicleIdOrderByRecordedAtDesc(vehicle.id())
                .map(TelemetryRecord::getOdometer)
                .orElse((long) random.nextInt(10000, 80000));

        long odometer = previousOdometer + random.nextInt(1, 21);

        return new TelemetryRequest(
                vehicle.id(),
                vehicle.vin(),
                speed,
                temperature,
                fuel,
                voltage,
                odometer,
                rpm,
                frontLeft,
                frontRight,
                rearLeft,
                rearRight
        );
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public TelemetryResponse getLatest(Long vehicleId) {
        TelemetryRecord record = repository
                .findFirstByVehicleIdOrderByRecordedAtDesc(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No telemetry found for vehicle " + vehicleId
                ));

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<TelemetryResponse> getHistory(
            Long vehicleId,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }

        return repository
                .findByVehicleIdOrderByRecordedAtDesc(
                        vehicleId,
                        PageRequest.of(page, size)
                )
                .map(this::toResponse);
    }

    private void validateTelemetry(TelemetryRequest request) {

        if (request.odometer() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Odometer cannot be negative"
            );
        }

        if (request.tyrePressureFrontLeft().signum() == 0
                || request.tyrePressureFrontRight().signum() == 0
                || request.tyrePressureRearLeft().signum() == 0
                || request.tyrePressureRearRight().signum() == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tyre pressure must be greater than zero"
            );
        }
    }

    private TelemetryResponse toResponse(TelemetryRecord record) {
        return new TelemetryResponse(
                record.getId(),
                record.getEventId(),
                record.getVehicleId(),
                record.getVin(),
                record.getSpeed(),
                record.getEngineTemperature(),
                record.getFuelLevel(),
                record.getBatteryVoltage(),
                record.getOdometer(),
                record.getRpm(),
                record.getTyrePressureFrontLeft(),
                record.getTyrePressureFrontRight(),
                record.getTyrePressureRearLeft(),
                record.getTyrePressureRearRight(),
                record.getRecordedAt()
        );
    }
}