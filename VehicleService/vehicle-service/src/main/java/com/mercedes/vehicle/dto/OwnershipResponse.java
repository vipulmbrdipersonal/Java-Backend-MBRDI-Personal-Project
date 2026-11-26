package com.mercedes.vehicle.dto;
import com.mercedes.vehicle.entity.VehicleOwnership;
import java.time.Instant;
public record OwnershipResponse(Long id,Long vehicleDatabaseId,String vehicleId,String ownerId,String ownerEmail,String purchaseId,Instant ownedFrom,Instant ownedUntil,boolean currentOwner) {
 public static OwnershipResponse from(VehicleOwnership o){return new OwnershipResponse(o.getId(),o.getVehicleId(),o.getVehicleCode(),o.getOwnerId(),o.getOwnerEmail(),o.getPurchaseId(),o.getOwnedFrom(),o.getOwnedUntil(),o.isCurrentOwner());}
}
