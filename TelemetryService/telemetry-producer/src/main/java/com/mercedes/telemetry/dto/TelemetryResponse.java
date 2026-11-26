
package com.mercedes.telemetry.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TelemetryResponse(
        Long id,
        String eventId,
        Long vehicleId,
        String vin,
        BigDecimal speed,
        BigDecimal engineTemperature,
        BigDecimal fuelLevel,
        BigDecimal batteryVoltage,
        Long odometer,
        BigDecimal rpm,
        BigDecimal tyrePressureFrontLeft,
        BigDecimal tyrePressureFrontRight,
        BigDecimal tyrePressureRearLeft,
        BigDecimal tyrePressureRearRight,
        LocalDateTime recordedAt
) {}
