
package com.mercedes.dashboard.dto;

import java.time.Instant;
import java.util.List;

public class VehicleDashboardResponse {

    private Long vehicleId;
    private String vin;
    private Integer healthScore;
    private String healthStatus;
    private List<String> issues;
    private Instant lastAssessedAt;

    public VehicleDashboardResponse(
            Long vehicleId,
            String vin,
            Integer healthScore,
            String healthStatus,
            List<String> issues,
            Instant lastAssessedAt) {
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.healthScore = healthScore;
        this.healthStatus = healthStatus;
        this.issues = issues;
        this.lastAssessedAt = lastAssessedAt;
    }

    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public Integer getHealthScore() { return healthScore; }
    public String getHealthStatus() { return healthStatus; }
    public List<String> getIssues() { return issues; }
    public Instant getLastAssessedAt() { return lastAssessedAt; }
}
