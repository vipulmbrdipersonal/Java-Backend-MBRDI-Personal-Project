
package com.mercedes.vehicle.dto;

import com.mercedes.vehicle.entity.Vehicle;
import com.mercedes.vehicle.entity.VehicleStatus;

import java.time.Instant;

public record VehicleResponse(
        Long id,
        String vehicleId,
        String vin,
        String make,
        String model,
        Integer modelYear,
        String modelId,
        String selectedColor,
        String ownerId,
        String ownerEmail,
        VehicleStatus status,
        String purchaseId,
        Instant createdAt,
        Instant updatedAt
) {

    public static VehicleResponse from(Vehicle vehicle) {

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleCode(),
                vehicle.getVin(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getModelYear(),
                vehicle.getModelId(),
                vehicle.getSelectedColor(),
                vehicle.getCurrentOwnerId(),
                vehicle.getCurrentOwnerEmail(),
                vehicle.getStatus(),
                vehicle.getPurchaseId(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
