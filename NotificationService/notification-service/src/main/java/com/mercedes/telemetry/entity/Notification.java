
package com.mercedes.telemetry.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_source_issue",
                columnNames = {"source_event_id", "issue_index"}
        ),
        indexes = {
                @Index(name = "idx_notification_vehicle_created",
                        columnList = "vehicle_id, created_at"),
                @Index(name = "idx_notification_vehicle_read",
                        columnList = "vehicle_id, is_read")
        }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_event_id", nullable = false, length = 100)
    private String sourceEventId;

    @Column(name = "issue_index", nullable = false)
    private Integer issueIndex;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(nullable = false, length = 30)
    private String vin;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(
            String sourceEventId,
            Integer issueIndex,
            Long vehicleId,
            String vin,
            String severity,
            String title,
            String message,
            Instant createdAt) {
        this.sourceEventId = sourceEventId;
        this.issueIndex = issueIndex;
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.read = false;
    }

    public void markAsRead() {
        this.read = true;
    }

    public Long getId() { return id; }
    public String getSourceEventId() { return sourceEventId; }
    public Integer getIssueIndex() { return issueIndex; }
    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
