
package com.mercedes.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_health_snapshots")
public class VehicleHealthSnapshot {

    @Id
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "telemetry_event_id")
    private String telemetryEventId;

    @Column(name = "vin")
    private String vin;

    @Column(name = "health_score")
    private Integer healthScore;

    @Column(name = "status")
    private String status;

    @Column(name = "issue_summary", columnDefinition = "TEXT")
    private String issueSummary;

    @Column(name = "assessed_at")
    private LocalDateTime assessedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Required by JPA
    protected VehicleHealthSnapshot() {
    }

    // Used when creating a new snapshot
    public VehicleHealthSnapshot(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getTelemetryEventId() {
        return telemetryEventId;
    }

    public void setTelemetryEventId(String telemetryEventId) {
        this.telemetryEventId = telemetryEventId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String issueSummary) {
        this.issueSummary = issueSummary;
    }

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
