
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByVehicleIdOrderByCreatedAtDesc(
            Long vehicleId, Pageable pageable);

    Page<Notification> findByVehicleIdAndReadOrderByCreatedAtDesc(
            Long vehicleId, boolean read, Pageable pageable);

    long countByVehicleIdAndReadFalse(Long vehicleId);

    boolean existsBySourceEventIdAndIssueIndex(
            String sourceEventId, Integer issueIndex);
}
