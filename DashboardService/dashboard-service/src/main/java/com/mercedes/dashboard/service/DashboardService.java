
package com.mercedes.dashboard.service;

import com.mercedes.dashboard.dto.*;
import com.mercedes.dashboard.entity.DashboardNotification;
import com.mercedes.dashboard.entity.VehicleHealthSnapshot;
import com.mercedes.dashboard.repository.DashboardNotificationRepository;
import com.mercedes.dashboard.repository.VehicleHealthSnapshotRepository;

import java.time.ZoneOffset;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final VehicleHealthSnapshotRepository healthRepository;
    private final DashboardNotificationRepository notificationRepository;

    public DashboardService(
            VehicleHealthSnapshotRepository healthRepository,
            DashboardNotificationRepository notificationRepository) {

        this.healthRepository = healthRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {

        List<VehicleDashboardResponse> vehicles =
                healthRepository.findAllByOrderByAssessedAtDesc()
                        .stream()
                        .map(this::toVehicleResponse)
                        .toList();

        long healthy = healthRepository.countByStatus("HEALTHY");
        long warning = healthRepository.countByStatus("WARNING");
        long critical = healthRepository.countByStatus("CRITICAL");

        long unread = notificationRepository.countByReadFalse();

        return new DashboardSummaryResponse(
                vehicles.size(),
                healthy,
                warning,
                critical,
                unread,
                vehicles
        );
    }

    @Transactional(readOnly = true)
    public VehicleDashboardResponse getVehicle(Long vehicleId) {

        VehicleHealthSnapshot snapshot =
                healthRepository.findById(vehicleId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Dashboard data not found for vehicle " + vehicleId
                        ));

        return toVehicleResponse(snapshot);
    }

    @Transactional(readOnly = true)
    public List<DashboardNotificationResponse> getNotifications(
            Long vehicleId, int page, int size) {

        return notificationRepository
                .findByVehicleIdOrderByCreatedAtDesc(
                        vehicleId,
                        PageRequest.of(
                                Math.max(page, 0),
                                Math.min(Math.max(size, 1), 100)
                        ))
                .map(this::toNotificationResponse)
                .getContent();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long vehicleId) {

        return notificationRepository.countByVehicleIdAndReadFalse(vehicleId);
    }

    private VehicleDashboardResponse toVehicleResponse(
            VehicleHealthSnapshot snapshot) {

        List<String> issues =
                snapshot.getIssueSummary() == null
                        || snapshot.getIssueSummary().isBlank()
                        ? List.of()
                        : List.of(snapshot.getIssueSummary().split("; "));

        return new VehicleDashboardResponse(
                snapshot.getVehicleId(),
                snapshot.getVin(),
                snapshot.getHealthScore(),
                snapshot.getStatus(),
                issues,
                snapshot.getAssessedAt() == null
                        ? null
                        : snapshot.getAssessedAt().toInstant(ZoneOffset.UTC)
        );
    }

    private DashboardNotificationResponse toNotificationResponse(
            DashboardNotification notification) {

        return new DashboardNotificationResponse(
                notification.getId(),
                notification.getVehicleId(),
                notification.getVin(),
                notification.getSeverity(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
