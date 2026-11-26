
package com.mercedes.dashboard.repository;

import com.mercedes.dashboard.entity.DashboardNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardNotificationRepository
        extends JpaRepository<DashboardNotification, Long> {

    Page<DashboardNotification> findByVehicleIdOrderByCreatedAtDesc(
            Long vehicleId, Pageable pageable);

    long countByVehicleIdAndReadFalse(Long vehicleId);

    long countByReadFalse();

    boolean existsBySourceNotificationId(Long sourceNotificationId);
}
