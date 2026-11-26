
package com.mercedes.telemetry.dto;

import java.time.Instant;
import java.util.List;

public class HealthResponse {

    private Long id;
    private Long vehicleId;
    private String vin;
    private String telemetryEventId;
    private Integer healthScore;
    private String status;
    private List<String> issues;
    private Instant assessedAt;

    public HealthResponse() {
    }

    public HealthResponse(Long id, Long vehicleId, String vin,
                          String telemetryEventId, Integer healthScore,
                          String status, List<String> issues,
                          Instant assessedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.telemetryEventId = telemetryEventId;
        this.healthScore = healthScore;
        this.status = status;
        this.issues = issues;
        this.assessedAt = assessedAt;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public String getTelemetryEventId() { return telemetryEventId; }
    public Integer getHealthScore() { return healthScore; }
    public String getStatus() { return status; }
    public List<String> getIssues() { return issues; }
    public Instant getAssessedAt() { return assessedAt; }
}
