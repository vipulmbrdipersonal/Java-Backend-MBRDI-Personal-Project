
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.TelemetryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelemetryRepository
        extends JpaRepository<TelemetryRecord, Long> {

    Page<TelemetryRecord> findByVehicleIdOrderByRecordedAtDesc(
            Long vehicleId, Pageable pageable);

    Optional<TelemetryRecord> findFirstByVehicleIdOrderByRecordedAtDesc(
            Long vehicleId);
}
