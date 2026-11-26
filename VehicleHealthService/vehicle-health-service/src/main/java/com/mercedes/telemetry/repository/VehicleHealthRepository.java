
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.VehicleHealthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleHealthRepository
        extends JpaRepository<VehicleHealthRecord, Long> {

    Optional<VehicleHealthRecord> findTopByVehicleIdOrderByAssessedAtDesc(
            Long vehicleId);

    Page<VehicleHealthRecord> findByVehicleIdOrderByAssessedAtDesc(
            Long vehicleId, Pageable pageable);

    boolean existsByTelemetryEventId(String telemetryEventId);
}
