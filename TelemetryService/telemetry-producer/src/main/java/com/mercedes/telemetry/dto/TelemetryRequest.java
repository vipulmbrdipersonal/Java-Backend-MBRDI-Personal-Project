
package com.mercedes.telemetry.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TelemetryRequest(

        @NotNull @Positive
        Long vehicleId,

        @NotBlank
        String vin,

        @NotNull @DecimalMin("0.0") @DecimalMax("400.0")
        BigDecimal speed,

        @NotNull @DecimalMin("-40.0") @DecimalMax("250.0")
        BigDecimal engineTemperature,

        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal fuelLevel,

        @NotNull @DecimalMin("0.0") @DecimalMax("30.0")
        BigDecimal batteryVoltage,

        @NotNull @PositiveOrZero
        Long odometer,

        @NotNull @DecimalMin("0.0") @DecimalMax("10000.0")
        BigDecimal rpm,

        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal tyrePressureFrontLeft,

        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal tyrePressureFrontRight,

        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal tyrePressureRearLeft,

        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal tyrePressureRearRight
) {}
