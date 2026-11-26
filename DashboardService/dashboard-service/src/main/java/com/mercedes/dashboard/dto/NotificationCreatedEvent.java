
package com.mercedes.dashboard.dto;

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

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
