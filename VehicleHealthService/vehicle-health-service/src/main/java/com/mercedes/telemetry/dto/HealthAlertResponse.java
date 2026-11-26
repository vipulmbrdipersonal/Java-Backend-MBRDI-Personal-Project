
package com.mercedes.telemetry.dto;

import java.time.Instant;

public class HealthAlertResponse {

    private Long id;
    private Long vehicleId;
    private String vin;
    private String severity;
    private String message;
    private Instant createdAt;

    public HealthAlertResponse(Long id, Long vehicleId, String vin,
                               String severity, String message,
                               Instant createdAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.severity = severity;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
