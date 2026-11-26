
package com.mercedes.telemetry.dto;

import java.time.Instant;

public class NotificationCreatedEvent {

    private String eventId;
    private Long notificationId;
    private Long vehicleId;
    private String vin;
    private String severity;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;

    public NotificationCreatedEvent() {
    }

    public NotificationCreatedEvent(
            String eventId,
            Long notificationId,
            Long vehicleId,
            String vin,
            String severity,
            String title,
            String message,
            boolean read,
            Instant createdAt) {
        this.eventId = eventId;
        this.notificationId = notificationId;
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public String getEventId() { return eventId; }
    public Long getNotificationId() { return notificationId; }
    public Long getVehicleId() { return vehicleId; }
    public String getVin() { return vin; }
    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
