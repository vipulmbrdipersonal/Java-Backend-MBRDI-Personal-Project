
package com.mercedes.telemetry.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "health_alerts",
        indexes = @Index(name = "idx_alert_vehicle_created",
                columnList = "vehicle_id, created_at"))
public class HealthAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(nullable = false, length = 30)
    private String vin;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "telemetry_event_id", nullable = false)
    private String telemetryEventId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HealthAlert() {
    }

    public HealthAlert(Long vehicleId, String vin, String severity,
                       String message, String telemetryEventId,
                       Instant createdAt) {
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.severity = severity;
        this.message = message;
        this.telemetryEventId = telemetryEventId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getTelemetryEventId() { return telemetryEventId; }
    public Instant getCreatedAt() { return createdAt; }
}
