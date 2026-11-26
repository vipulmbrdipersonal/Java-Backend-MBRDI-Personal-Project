
package com.mercedes.dashboard.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "dashboard_notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dashboard_notification_source",
                columnNames = "source_notification_id"
        ),
        indexes = {
                @Index(name = "idx_dashboard_notification_vehicle_created",
                        columnList = "vehicle_id, created_at"),
                @Index(name = "idx_dashboard_notification_unread",
                        columnList = "vehicle_id, is_read")
        }
)
public class DashboardNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_notification_id", nullable = false, unique = true)
    private Long sourceNotificationId;

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
    private boolean read;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DashboardNotification() {
    }

    public DashboardNotification(
            Long sourceNotificationId,
            Long vehicleId,
            String vin,
            String severity,
            String title,
            String message,
            boolean read,
            Instant createdAt) {
        this.sourceNotificationId = sourceNotificationId;
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public void markAsRead() {
        this.read = true;
    }

    public Long getId() { return id; }
    public Long getSourceNotificationId() { return sourceNotificationId; }
    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
