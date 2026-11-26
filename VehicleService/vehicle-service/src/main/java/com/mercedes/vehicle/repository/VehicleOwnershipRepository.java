package com.mercedes.vehicle.repository;
import com.mercedes.vehicle.entity.VehicleOwnership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VehicleOwnershipRepository extends JpaRepository<VehicleOwnership,Long> {
 List<VehicleOwnership> findByVehicleIdOrderByOwnedFromDesc(Long vehicleId);
 List<VehicleOwnership> findByOwnerIdOrderByOwnedFromDesc(String ownerId);
 Optional<VehicleOwnership> findByVehicleIdAndCurrentOwnerTrue(Long vehicleId);
}
