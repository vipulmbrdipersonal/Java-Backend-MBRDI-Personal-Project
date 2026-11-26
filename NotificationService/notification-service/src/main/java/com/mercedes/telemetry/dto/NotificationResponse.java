
package com.mercedes.telemetry.dto;

import java.time.Instant;

public class NotificationResponse {

    private Long id;
    private Long vehicleId;
    private String vin;
    private String severity;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;

    public NotificationResponse(
            Long id,
            Long vehicleId,
            String vin,
            String severity,
            String title,
            String message,
            boolean read,
            Instant createdAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
