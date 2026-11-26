
package com.mercedes.dashboard.dto;

import java.time.Instant;
import java.util.List;

public class VehicleHealthEvent {

    private String eventId;
    private String telemetryEventId;
    private Long vehicleId;
    private String vin;
    private Integer healthScore;
    private String status;
    private List<String> issues;
    private Instant assessedAt;

    public VehicleHealthEvent() {
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTelemetryEventId() { return telemetryEventId; }
    public void setTelemetryEventId(String value) {
        this.telemetryEventId = value;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Integer getHealthScore() { return healthScore; }
    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getIssues() { return issues; }
    public void setIssues(List<String> issues) { this.issues = issues; }

    public Instant getAssessedAt() { return assessedAt; }
    public void setAssessedAt(Instant assessedAt) {
        this.assessedAt = assessedAt;
    }
}
