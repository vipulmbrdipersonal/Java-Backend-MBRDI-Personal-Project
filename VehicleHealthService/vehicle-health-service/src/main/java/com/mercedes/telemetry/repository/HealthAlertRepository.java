
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.HealthAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthAlertRepository
        extends JpaRepository<HealthAlert, Long> {

    Page<HealthAlert> findByVehicleIdOrderByCreatedAtDesc(
            Long vehicleId, Pageable pageable);
}
