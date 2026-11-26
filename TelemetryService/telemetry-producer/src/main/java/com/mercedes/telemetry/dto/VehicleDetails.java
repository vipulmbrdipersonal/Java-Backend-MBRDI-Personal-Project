package com.mercedes.telemetry.dto;

public record VehicleDetails(
        Long id,
        String vehicleId,
        String vin,
        String ownerEmail
) {
}