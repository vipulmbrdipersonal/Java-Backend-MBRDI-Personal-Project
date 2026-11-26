
package com.mercedes.vehicle.repository;

import com.mercedes.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVehicleCode(String vehicleCode);

    Optional<Vehicle> findByVin(String vin);

    List<Vehicle> findByCurrentOwnerIdOrderByCreatedAtDesc(
            String ownerId
    );

    Optional<Vehicle> findByIdAndCurrentOwnerId(
            Long id,
            String ownerId
    );

    boolean existsByVin(String vin);

    boolean existsByPurchaseId(String purchaseId);
}
