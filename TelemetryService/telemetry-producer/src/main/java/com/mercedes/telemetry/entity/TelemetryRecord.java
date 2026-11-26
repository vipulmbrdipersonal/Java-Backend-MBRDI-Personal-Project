
package com.mercedes.telemetry.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "vehicle_telemetry",
        indexes = {
                @Index(name = "idx_telemetry_vehicle_time",
                        columnList = "vehicleId,recordedAt"),
                @Index(name = "idx_telemetry_vin", columnList = "vin")
        }
)
public class TelemetryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String eventId;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false, length = 30)
    private String vin;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal speed;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal engineTemperature;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal fuelLevel;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal batteryVoltage;

    @Column(nullable = false)
    private Long odometer;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rpm;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tyrePressureFrontLeft;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tyrePressureFrontRight;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tyrePressureRearLeft;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tyrePressureRearRight;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    public void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }

    public String getEventId() { return eventId; }
    public void setEventId(String value) { eventId = value; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long value) { vehicleId = value; }

    public String getVin() { return vin; }
    public void setVin(String value) { vin = value; }

    public BigDecimal getSpeed() { return speed; }
    public void setSpeed(BigDecimal value) { speed = value; }

    public BigDecimal getEngineTemperature() { return engineTemperature; }
    public void setEngineTemperature(BigDecimal value) {
        engineTemperature = value;
    }

    public BigDecimal getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(BigDecimal value) { fuelLevel = value; }

    public BigDecimal getBatteryVoltage() { return batteryVoltage; }
    public void setBatteryVoltage(BigDecimal value) { batteryVoltage = value; }

    public Long getOdometer() { return odometer; }
    public void setOdometer(Long value) { odometer = value; }

    public BigDecimal getRpm() { return rpm; }
    public void setRpm(BigDecimal value) { rpm = value; }

    public BigDecimal getTyrePressureFrontLeft() {
        return tyrePressureFrontLeft;
    }
    public void setTyrePressureFrontLeft(BigDecimal value) {
        tyrePressureFrontLeft = value;
    }

    public BigDecimal getTyrePressureFrontRight() {
        return tyrePressureFrontRight;
    }
    public void setTyrePressureFrontRight(BigDecimal value) {
        tyrePressureFrontRight = value;
    }

    public BigDecimal getTyrePressureRearLeft() {
        return tyrePressureRearLeft;
    }
    public void setTyrePressureRearLeft(BigDecimal value) {
        tyrePressureRearLeft = value;
    }

    public BigDecimal getTyrePressureRearRight() {
        return tyrePressureRearRight;
    }
    public void setTyrePressureRearRight(BigDecimal value) {
        tyrePressureRearRight = value;
    }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime value) {
        recordedAt = value;
    }
}
