
package com.mercedes.telemetry.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "vehicle_health_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_health_telemetry_event",
                columnNames = "telemetry_event_id"
        ),
        indexes = {
                @Index(
                        name = "idx_health_vehicle_assessed",
                        columnList = "vehicle_id, assessed_at"
                )
        }
)
public class VehicleHealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telemetry_event_id", nullable = false, unique = true)
    private String telemetryEventId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(nullable = false, length = 30)
    private String vin;

    @Column(nullable = false)
    private Integer healthScore;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "issue_summary", columnDefinition = "TEXT")
    private String issueSummary;

    @Column(name = "engine_temperature")
    private Double engineTemperature;

    @Column(name = "battery_voltage")
    private Double batteryVoltage;

    @Column(name = "fuel_level")
    private Double fuelLevel;

    @Column(name = "tyre_pressure_front_left")
    private Double tyrePressureFrontLeft;

    @Column(name = "tyre_pressure_front_right")
    private Double tyrePressureFrontRight;

    @Column(name = "tyre_pressure_rear_left")
    private Double tyrePressureRearLeft;

    @Column(name = "tyre_pressure_rear_right")
    private Double tyrePressureRearRight;

    @Column(name = "rpm")
    private Integer rpm;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    // Public no-argument constructor
    public VehicleHealthRecord() {
    }

    public Long getId() {
        return id;
    }

    public String getTelemetryEventId() {
        return telemetryEventId;
    }

    public void setTelemetryEventId(String value) {
        this.telemetryEventId = value;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long value) {
        this.vehicleId = value;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String value) {
        this.vin = value;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer value) {
        this.healthScore = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String value) {
        this.issueSummary = value;
    }

    public Double getEngineTemperature() {
        return engineTemperature;
    }

    public void setEngineTemperature(Double value) {
        this.engineTemperature = value;
    }

    public Double getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(Double value) {
        this.batteryVoltage = value;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double value) {
        this.fuelLevel = value;
    }

    public Double getTyrePressureFrontLeft() {
        return tyrePressureFrontLeft;
    }

    public void setTyrePressureFrontLeft(Double value) {
        this.tyrePressureFrontLeft = value;
    }

    public Double getTyrePressureFrontRight() {
        return tyrePressureFrontRight;
    }

    public void setTyrePressureFrontRight(Double value) {
        this.tyrePressureFrontRight = value;
    }

    public Double getTyrePressureRearLeft() {
        return tyrePressureRearLeft;
    }

    public void setTyrePressureRearLeft(Double value) {
        this.tyrePressureRearLeft = value;
    }

    public Double getTyrePressureRearRight() {
        return tyrePressureRearRight;
    }

    public void setTyrePressureRearRight(Double value) {
        this.tyrePressureRearRight = value;
    }

    public Integer getRpm() {
        return rpm;
    }

    public void setRpm(Integer value) {
        this.rpm = value;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(Instant value) {
        this.assessedAt = value;
    }
}
